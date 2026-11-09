package com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class DistributorProduce{
    public DistributorProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    private final OutboxStore outbox;


    public void publish(DistributorCreatedEvent event) {

        outbox.enqueue(
                "distributor-created",
                event.idDistributor().toString(),
                event
        );

    }

    public void publishMetrics(DistributorCreatedEvent event) {

        outbox.enqueue(
                "distributor-created-metrics",
                event.idDistributor().toString(),
                event
        );

    }

}
