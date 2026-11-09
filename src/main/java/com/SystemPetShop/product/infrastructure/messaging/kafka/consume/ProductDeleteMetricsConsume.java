package com.SystemPetShop.product.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductDeleteMetricsConsume {

    private final ProductMetricsRepository productMetricsRepository;

    public ProductDeleteMetricsConsume(ProductMetricsRepository productMetricsRepository) {
        this.productMetricsRepository = productMetricsRepository;
    }

    @KafkaListener(
            topics = "product-delete-metrics",
            groupId = "product-metrics-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "metricsTransactionManager")
    public void deleteProductMetrics(ProductDeleteEvent event) {
        productMetricsRepository.deleteById(event.idProduct());
    }
}
