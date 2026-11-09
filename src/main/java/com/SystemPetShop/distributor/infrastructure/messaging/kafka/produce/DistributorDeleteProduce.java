package com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.distributor.infrastructure.messaging.kafka.dto.DistributorDeleteEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class DistributorDeleteProduce {

    private final OutboxStore outbox;

    public DistributorDeleteProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void delete(DistributorDeleteEvent idDistributor){
        outbox.enqueue(
                "distributor-delete",
                idDistributor.toString(),
                idDistributor
        );
    }

    public void deleteMetrics(DistributorDeleteEvent idDistributor){
        outbox.enqueue(
                "distributor-delete-metrics",
                idDistributor.toString(),
                idDistributor
        );
    }
}
