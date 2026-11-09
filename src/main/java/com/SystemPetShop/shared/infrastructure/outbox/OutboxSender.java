package com.SystemPetShop.shared.infrastructure.outbox;

@FunctionalInterface
public interface OutboxSender {
    // Return only after the broker acknowledges the record.
    void send(OutboxMessage message) throws Exception;
}
