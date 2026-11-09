package com.SystemPetShop.distributor.application.port;

import com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent;

import java.util.concurrent.CompletionStage;

public interface DistributorEventPublisher {
    CompletionStage<Void> publish(DistributorCreatedEvent event);
}
