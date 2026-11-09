package com.SystemPetShop.distributor.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.distributor.infrastructure.messaging.kafka.dto.DistributorDeleteEvent;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.repository.DistributorExpiredRepositoryEntity;
import com.SystemPetShop.distributor.infrastructure.metrics.DistributorMetricsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DistributorDeleteConsume {
    private final DistributorExpiredRepositoryEntity distributorRepositoryEntity;
    private final DistributorMetricsRepository distributorMetricsRepository;

    public DistributorDeleteConsume(DistributorExpiredRepositoryEntity distributorRepositoryEntity,
                                    DistributorMetricsRepository distributorMetricsRepository) {
        this.distributorRepositoryEntity = distributorRepositoryEntity;
        this.distributorMetricsRepository = distributorMetricsRepository;
    }

    @KafkaListener(
            topics = "distributor-delete",
            groupId = "distributor-d-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.distributor.infrastructure.messaging.kafka.dto.DistributorDeleteEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void deleteDistributor(DistributorDeleteEvent id){
        distributorRepositoryEntity.deleteById(id.idDistributor());
    }

    @KafkaListener(
            topics = "distributor-delete-metrics",
            groupId = "distributor-metrics-delete-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.distributor.infrastructure.messaging.kafka.dto.DistributorDeleteEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "metricsTransactionManager")
    public void deleteDistributorMetrics(DistributorDeleteEvent id){
        distributorMetricsRepository.deleteById(id.idDistributor());
    }
}
