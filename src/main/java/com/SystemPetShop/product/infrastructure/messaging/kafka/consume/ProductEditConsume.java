package com.SystemPetShop.product.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.product.infrastructure.messaging.kafka.repository.ProductExpiredRepositoryEntity;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductEditConsume {
    private final ProductExpiredRepositoryEntity productExpiredRepositoryEntity;

    public ProductEditConsume(ProductExpiredRepositoryEntity productExpiredRepositoryEntity) {
        this.productExpiredRepositoryEntity = productExpiredRepositoryEntity;
    }

    @KafkaListener(
            topics = "product-update",
            groupId = "product-update-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void productEdit(ProductCreatedEvent event){
        if (productExpiredRepositoryEntity.editProduct(ProductCreatedEvent.toEntity(event)) == 0) {
            throw new IllegalStateException("Product not found in expired database");
        }
    }
}
