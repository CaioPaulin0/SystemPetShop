package com.SystemPetShop.product.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCreatedMetricsConsume {

    private final ProductMetricsRepository productMetricsRepository;

    public ProductCreatedMetricsConsume(ProductMetricsRepository productMetricsRepository) {
        this.productMetricsRepository = productMetricsRepository;
    }

    @KafkaListener(
            topics = "product-created-metrics",
            groupId = "product-metrics-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "metricsTransactionManager")
    public void createProductMetrics(ProductCreatedEvent event) {
        productMetricsRepository.save(ProductCreatedEvent.toEntity(event));
    }
}
