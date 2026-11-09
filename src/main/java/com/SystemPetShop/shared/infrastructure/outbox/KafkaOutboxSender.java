package com.SystemPetShop.shared.infrastructure.outbox;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaOutboxSender implements OutboxSender {
    private final DefaultKafkaProducerFactory<String, String> factory;
    private final KafkaTemplate<String, String> kafka;
    private final long timeoutMs;

    public KafkaOutboxSender(KafkaProperties properties,
                             @Value("${app.outbox.send-timeout-ms:10000}") long timeoutMs) {
        if (timeoutMs < 1 || timeoutMs > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid outbox send timeout");
        }
        this.timeoutMs = timeoutMs;
        var config = new HashMap<>(properties.buildProducerProperties());
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, timeoutMs);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int) timeoutMs);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, (int) timeoutMs);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        // Payload is already JSON. A JSON serializer here would double-encode it as a string.
        factory = new DefaultKafkaProducerFactory<>(config, new StringSerializer(), new StringSerializer());
        kafka = new KafkaTemplate<>(factory);
    }

    @Override
    public void send(OutboxMessage message) throws Exception {
        kafka.send(record(message)).get(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public static ProducerRecord<String, String> record(OutboxMessage message) {
        var record = new ProducerRecord<String, String>(message.topic(), message.messageKey(), message.payload());
        record.headers().add("outbox-event-id", message.eventId().getBytes(StandardCharsets.UTF_8));
        return record;
    }

    @PreDestroy
    public void close() {
        factory.destroy();
    }
}
