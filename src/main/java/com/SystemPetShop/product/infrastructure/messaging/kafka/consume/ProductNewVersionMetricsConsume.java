package com.SystemPetShop.product.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionMetricsEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionEvent;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductNewVersionMetricsConsume {

    private final ProductMetricsRepository productMetricsRepository;

    public ProductNewVersionMetricsConsume(ProductMetricsRepository productMetricsRepository) {
        this.productMetricsRepository = productMetricsRepository;
    }

    @KafkaListener(
            topics = "product-new-version-metrics",
            groupId = "product-metrics-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionMetricsEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "metricsTransactionManager")
    public void newVersionProductMetrics(ProductNewVersionMetricsEvent event) {
        if (event.previousProductId() == null || event.product() == null
                || event.previousProductId().equals(event.product().idProduct())) {
            throw new IllegalArgumentException("Previous and new product versions must have different identifiers");
        }
        if (productMetricsRepository.disablePreviousVersion(event.previousProductId()) == 0) {
            throw new IllegalStateException("Previous product version not found in metrics database");
        }
        productMetricsRepository.save(ProductNewVersionEvent.toEntity(event.product()));
    }
}
