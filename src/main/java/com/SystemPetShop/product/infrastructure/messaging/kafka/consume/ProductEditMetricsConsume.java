package com.SystemPetShop.product.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductEditMetricsConsume {

    private final ProductMetricsRepository productMetricsRepository;

    public ProductEditMetricsConsume(ProductMetricsRepository productMetricsRepository) {
        this.productMetricsRepository = productMetricsRepository;
    }

    @KafkaListener(
            topics = "product-update-metrics",
            groupId = "product-metrics-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "metricsTransactionManager")
    public void editProductMetrics(ProductCreatedEvent event) {
        if (productMetricsRepository.editProduct(ProductCreatedEvent.toEntity(event)) == 0) {
            throw new IllegalStateException("Product not found in metrics database");
        }
    }
}
