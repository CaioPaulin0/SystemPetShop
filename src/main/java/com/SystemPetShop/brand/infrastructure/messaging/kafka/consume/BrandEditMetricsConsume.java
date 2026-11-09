package com.SystemPetShop.brand.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent;
import com.SystemPetShop.brand.infrastructure.metrics.BrandMetricsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandEditMetricsConsume {

    private final BrandMetricsRepository brandMetricsRepository;

    public BrandEditMetricsConsume(BrandMetricsRepository brandMetricsRepository) {
        this.brandMetricsRepository = brandMetricsRepository;
    }

    @KafkaListener(
            topics = "brand-update-metrics",
            groupId = "brand-metrics-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "metricsTransactionManager")
    public void editBrandMetrics(BrandCreatedEvent event) {
        brandMetricsRepository.editBrand(BrandCreatedEvent.toEntity(event));
    }
}
