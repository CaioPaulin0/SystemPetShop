package com.SystemPetShop.shared.infrastructure.outbox;

public record OutboxMessage(long id, String eventId, String topic, String messageKey,
                            String payload, int attempts) {
}
