package com.SystemPetShop.distributor.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.repository.DistributorExpiredRepositoryEntity;
import com.SystemPetShop.distributor.infrastructure.metrics.DistributorMetricsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DistributorConsume {

    private final DistributorExpiredRepositoryEntity distributorRepositoryEntity;
    private final DistributorMetricsRepository distributorMetricsRepository;

    public DistributorConsume(DistributorExpiredRepositoryEntity distributorRepositoryEntity,
                              DistributorMetricsRepository distributorMetricsRepository) {
        this.distributorRepositoryEntity = distributorRepositoryEntity;
        this.distributorMetricsRepository = distributorMetricsRepository;
    }

    @KafkaListener(
            topics = "distributor-created",
            groupId = "distributor-1",
    properties = {
        "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                "spring.json.value.default.type=com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent",
                "spring.json.use.type.headers=false"
    }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void receiveDistributor(DistributorCreatedEvent distributorCreatedEvent){

        DistributorEntity entity = new DistributorEntity(
                distributorCreatedEvent.idDistributor(),
                distributorCreatedEvent.nameDistributor(),
                distributorCreatedEvent.localizationDistributor(),
                distributorCreatedEvent.salespeopleDistributor(),
                distributorCreatedEvent.typeDistributor(),
                distributorCreatedEvent.telephones(),
                distributorCreatedEvent.CNPJ()
        );

        distributorRepositoryEntity.save(entity);

    }

    @KafkaListener(
            topics = "distributor-created-metrics",
            groupId = "distributor-metrics-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "metricsTransactionManager")
    public void receiveDistributorMetrics(DistributorCreatedEvent distributorCreatedEvent){

        DistributorEntity entity = new DistributorEntity(
                distributorCreatedEvent.idDistributor(),
                distributorCreatedEvent.nameDistributor(),
                distributorCreatedEvent.localizationDistributor(),
                distributorCreatedEvent.salespeopleDistributor(),
                distributorCreatedEvent.typeDistributor(),
                distributorCreatedEvent.telephones(),
                distributorCreatedEvent.CNPJ()
        );

        distributorMetricsRepository.save(entity);

    }

}
