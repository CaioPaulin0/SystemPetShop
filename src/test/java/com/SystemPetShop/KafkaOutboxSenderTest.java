package com.SystemPetShop;

import com.SystemPetShop.shared.infrastructure.outbox.KafkaOutboxSender;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaOutboxSenderTest {
    @Test void falhaAssincronaDoBrokerChegaAoPublicador() {
        try (var fixture = new Fixture()) {
            when(fixture.kafka.send(any(org.apache.kafka.clients.producer.ProducerRecord.class)))
                    .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Broker rejected")));
            assertThatThrownBy(() -> fixture.sender.send(message()))
                    .isInstanceOf(ExecutionException.class).hasCauseInstanceOf(IllegalStateException.class);
        }
    }

    @Test void ausenciaDeConfirmacaoNaoEhTratadaComoSucesso() {
        try (var fixture = new Fixture()) {
            when(fixture.kafka.send(any(org.apache.kafka.clients.producer.ProducerRecord.class)))
                    .thenReturn(new CompletableFuture<>());
            assertThatThrownBy(() -> fixture.sender.send(message())).isInstanceOf(TimeoutException.class);
        }
    }

    @Test void confirmacaoDoBrokerPermiteConcluirEnvio() throws Exception {
        try (var fixture = new Fixture()) {
            when(fixture.kafka.send(any(org.apache.kafka.clients.producer.ProducerRecord.class)))
                    .thenReturn(CompletableFuture.completedFuture(null));
            fixture.sender.send(message());
            verify(fixture.kafka).send(any(org.apache.kafka.clients.producer.ProducerRecord.class));
        }
    }

    private OutboxMessage message() { return new OutboxMessage(1, "event-id", "topic", "key", "{}", 0); }

    private static class Fixture implements AutoCloseable {
        @SuppressWarnings("unchecked")
        final KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
        final KafkaOutboxSender sender = new KafkaOutboxSender(new KafkaProperties(), 50);
        Fixture() { ReflectionTestUtils.setField(sender, "kafka", kafka); }
        @Override public void close() { sender.close(); }
    }
}
