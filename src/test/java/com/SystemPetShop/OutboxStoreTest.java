package com.SystemPetShop;

import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.shared.infrastructure.outbox.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

class OutboxStoreTest {
    JdbcDataSource dataSource;
    JdbcTemplate jdbc;
    JdbcTransactionManager manager;
    TransactionTemplate transaction;
    OutboxStore store;
    MutableClock clock;

    @BeforeEach void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:outbox_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(new ClassPathResource("db/manual/2026-11-04-outbox.sql")).execute(dataSource);
        jdbc = new JdbcTemplate(dataSource);
        manager = new JdbcTransactionManager(dataSource);
        transaction = new TransactionTemplate(manager);
        clock = new MutableClock();
        store = new OutboxStore(dataSource, manager, clock, 1000, 4000);
    }

    @Test void exigeTransacaoDoBancoDeOrigem() {
        assertThatThrownBy(() -> store.enqueue("topic", "key", "event"))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(count()).isZero();
    }

    @Test void rollbackRemoveEventoPendente() {
        transaction.executeWithoutResult(status -> {
            store.enqueue("topic", "key", "event");
            assertThat(count()).isEqualTo(1);
            status.setRollbackOnly();
        });
        assertThat(count()).isZero();
    }

    @Test void falhaDeSerializacaoReverteTransacao() {
        assertThatThrownBy(() -> transaction.executeWithoutResult(status ->
                store.enqueue("topic", "key", new BrokenPayload())))
                .isInstanceOf(RuntimeException.class);
        assertThat(count()).isZero();
    }

    @Test void naoPublicaAntesDoCommit() throws Exception {
        try (var executor = Executors.newSingleThreadExecutor()) {
            transaction.executeWithoutResult(status -> {
                store.enqueue("topic", "key", "event");
                try {
                    assertThat(executor.submit(() -> store.publishNext(message -> {
                        throw new AssertionError("Uncommitted event published");
                    })).get(5, TimeUnit.SECONDS)).isFalse();
                } catch (Exception e) { throw new RuntimeException(e); }
            });
        }
        assertThat(store.publishNext(message -> {})).isTrue();
    }

    @Test void publicaJsonCompativelComConsumidorAtualEMarcaSomenteAposConfirmacao() {
        var date = LocalDate.of(2026, 11, 4);
        var product = new Product(UUID.randomUUID(), UUID.randomUUID(), "Racao", "Alimento", "Descricao",
                date, date.plusYears(1), "001-A", 10, 20, 5, false, 1, true);
        var event = ProductCreatedEvent.toEvent(product);
        transaction.executeWithoutResult(status -> store.enqueue("product-created", event.idProduct().toString(), event));
        assertThat(store.publishNext(message -> {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event WHERE published_at IS NOT NULL", Integer.class)).isZero();
            var record = KafkaOutboxSender.record(message);
            assertThat(record.key()).isEqualTo(event.idProduct().toString());
            assertThat(new String(record.headers().lastHeader("outbox-event-id").value(), StandardCharsets.UTF_8))
                    .isEqualTo(message.eventId());
            try (var deserializer = new JacksonJsonDeserializer<>(ProductCreatedEvent.class, false)) {
                assertThat(deserializer.deserialize(record.topic(), record.value().getBytes(StandardCharsets.UTF_8)))
                        .isEqualTo(event);
            }
        })).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event WHERE published_at IS NOT NULL", Integer.class)).isEqualTo(1);
        assertThat(store.publishNext(message -> { throw new AssertionError("Already published"); })).isFalse();
    }

    @Test void falhaMantemEventoEReinicioRetomaComMesmoIdentificador() {
        enqueue();
        List<String> ids = new ArrayList<>();
        assertThat(store.publishNext(message -> {
            ids.add(message.eventId());
            throw new TimeoutException("broker unavailable");
        })).isTrue();
        assertThat(jdbc.queryForObject("SELECT attempts FROM outbox_event", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT last_error FROM outbox_event", String.class)).isEqualTo("TimeoutException");
        assertThat(store.publishNext(message -> { throw new AssertionError("Retry too early"); })).isFalse();
        clock.advance(1000);
        var restarted = new OutboxStore(dataSource, manager, clock, 1000, 4000);
        assertThat(restarted.publishNext(message -> ids.add(message.eventId()))).isTrue();
        assertThat(ids).hasSize(2);
        assertThat(ids.get(0)).isEqualTo(ids.get(1));
    }

    @Test void retentativasTemAtrasoExponencialLimitadoSemDescartarEvento() {
        enqueue();
        for (long delay : new long[]{1000, 2000, 4000, 4000}) {
            assertThat(store.publishNext(message -> { throw new IllegalStateException(); })).isTrue();
            var nextAttempt = jdbc.queryForObject("SELECT next_attempt_at FROM outbox_event", LocalDateTime.class);
            assertThat(nextAttempt).isEqualTo(LocalDateTime.now(clock).plusNanos(delay * 1_000_000));
            clock.advance(delay);
        }
        assertThat(count()).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT attempts FROM outbox_event", Integer.class)).isEqualTo(4);
    }

    @Test void publicadoresConcorrentesNaoEnviamMesmaLinhaSimultaneamente() throws Exception {
        enqueue();
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> store.publishNext(message -> {
                entered.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("Test release timed out");
            }));
            try {
                assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(executor.submit(() -> store.publishNext(message -> {
                    throw new AssertionError("Concurrent duplicate");
                })).get(5, TimeUnit.SECONDS)).isFalse();
            } finally { release.countDown(); }
            assertThat(first.get(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test void falhaNoCommitAposAckPermiteReenvioComMesmoEventId() {
        enqueue();
        var failOnce = new AtomicBoolean(true);
        var failingManager = new JdbcTransactionManager(dataSource) {
            @Override protected void doCommit(DefaultTransactionStatus status) {
                if (failOnce.getAndSet(false)) throw new TransactionSystemException("Simulated commit failure");
                super.doCommit(status);
            }
        };
        failingManager.setRollbackOnCommitFailure(true);
        var failingStore = new OutboxStore(dataSource, failingManager, clock, 1000, 4000);
        List<String> ids = new ArrayList<>();
        assertThatThrownBy(() -> failingStore.publishNext(message -> ids.add(message.eventId())))
                .isInstanceOf(TransactionSystemException.class);
        assertThat(store.publishNext(message -> ids.add(message.eventId()))).isTrue();
        assertThat(ids).hasSize(2);
        assertThat(ids.getFirst()).isEqualTo(ids.getLast());
    }

    private void enqueue() {
        transaction.executeWithoutResult(status -> store.enqueue("topic", "key", "event"));
    }
    private int count() { return jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event", Integer.class); }
    static class BrokenPayload {
        public String getValue() { throw new IllegalStateException("Cannot serialize"); }
    }
    static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-04T00:00:00Z");
        void advance(long millis) { now = now.plusMillis(millis); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        @Override public Instant instant() { return now; }
    }
}
