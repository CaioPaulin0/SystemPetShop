package com.SystemPetShop.shared.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.outbox.publisher-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final List<OutboxStore> stores;
    private final OutboxSender sender;
    private final int batchSize;

    public OutboxPublisher(List<OutboxStore> stores, OutboxSender sender,
                           @Value("${app.outbox.batch-size:20}") int batchSize) {
        if (batchSize < 1) throw new IllegalArgumentException("Outbox batch size must be positive");
        this.stores = List.copyOf(stores);
        this.sender = sender;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:1000}",
               initialDelayString = "${app.outbox.poll-delay-ms:1000}")
    public void publishPending() {
        for (var store : stores) {
            try {
                for (int i = 0; i < batchSize && !Thread.currentThread().isInterrupted(); i++) {
                    if (!store.publishNext(sender)) break;
                }
            } catch (RuntimeException failure) {
                // A database outage must not permanently stop scheduling or the other database's queue.
                log.error("Outbox database dispatch failed; pending events will be retried", failure);
            }
        }
    }
}
