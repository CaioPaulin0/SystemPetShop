package com.SystemPetShop.shared.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/** One store per source database; JDBC participates in that database's JPA transaction. */
public class OutboxStore {
    private static final Logger log = LoggerFactory.getLogger(OutboxStore.class);
    private final JdbcTemplate jdbc;
    private final TransactionTemplate enqueueTransaction;
    private final TransactionTemplate publishTransaction;
    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
    private final Clock clock;
    private final long retryDelayMs;
    private final long maxRetryDelayMs;

    public OutboxStore(DataSource dataSource, PlatformTransactionManager transactionManager,
                       Clock clock, long retryDelayMs, long maxRetryDelayMs) {
        if (retryDelayMs < 1 || maxRetryDelayMs < retryDelayMs) {
            throw new IllegalArgumentException("Invalid outbox retry delays");
        }
        this.jdbc = new JdbcTemplate(dataSource);
        this.clock = clock;
        this.retryDelayMs = retryDelayMs;
        this.maxRetryDelayMs = maxRetryDelayMs;
        this.enqueueTransaction = new TransactionTemplate(transactionManager);
        this.enqueueTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
        this.publishTransaction = new TransactionTemplate(transactionManager);
        this.publishTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void enqueue(String topic, String messageKey, Object event) {
        enqueueTransaction.executeWithoutResult(status -> {
            // Serialization errors must roll back the business write as well.
            String payload = mapper.writeValueAsString(event);
            LocalDateTime now = LocalDateTime.now(clock);
            jdbc.update("""
                    INSERT INTO outbox_event
                        (event_id, topic, message_key, payload, created_at, next_attempt_at, attempts)
                    VALUES (?, ?, ?, ?, ?, ?, 0)
                    """, UUID.randomUUID().toString(), topic, messageKey, payload, now, now);
        });
    }

    /** The row lock prevents concurrent publishers from dispatching this row simultaneously. */
    public boolean publishNext(OutboxSender sender) {
        return Boolean.TRUE.equals(publishTransaction.execute(status -> {
            var rows = jdbc.query("""
                    SELECT id, event_id, topic, message_key, payload, attempts
                    FROM outbox_event
                    WHERE published_at IS NULL AND next_attempt_at <= ?
                    ORDER BY next_attempt_at, id
                    LIMIT 1 FOR UPDATE SKIP LOCKED
                    """, (rs, row) -> new OutboxMessage(rs.getLong("id"), rs.getString("event_id"),
                    rs.getString("topic"), rs.getString("message_key"), rs.getString("payload"),
                    rs.getInt("attempts")), LocalDateTime.now(clock));
            if (rows.isEmpty()) return false;
            OutboxMessage message = rows.getFirst();
            try {
                sender.send(message);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Outbox publisher interrupted", interrupted);
            } catch (Exception failure) {
                int attempts = message.attempts() == Integer.MAX_VALUE ? Integer.MAX_VALUE : message.attempts() + 1;
                long delay = retryDelayMs;
                for (int i = 1; i < attempts && delay < maxRetryDelayMs; i++) {
                    delay = delay > maxRetryDelayMs / 2 ? maxRetryDelayMs : delay * 2;
                }
                jdbc.update("""
                        UPDATE outbox_event SET attempts = ?, next_attempt_at = ?, last_error = ? WHERE id = ?
                        """, attempts, LocalDateTime.now(clock).plus(Duration.ofMillis(delay)),
                        failure.getClass().getSimpleName(), message.id());
                log.warn("Outbox event {} on {} failed ({}), attempt {}. It remains pending.",
                        message.eventId(), message.topic(), failure.getClass().getSimpleName(), attempts);
                return true;
            }
            // If this commit fails after Kafka acknowledges, the row is retried with the SAME event ID.
            jdbc.update("UPDATE outbox_event SET published_at = ?, last_error = NULL WHERE id = ?",
                    LocalDateTime.now(clock), message.id());
            return true;
        }));
    }
}
