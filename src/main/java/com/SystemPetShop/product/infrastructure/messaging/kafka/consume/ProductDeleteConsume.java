package com.SystemPetShop.product.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.product.infrastructure.messaging.kafka.repository.ProductExpiredRepositoryEntity;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductDeleteConsume {
    private final ProductExpiredRepositoryEntity productExpiredRepositoryEntity;

    public ProductDeleteConsume(ProductExpiredRepositoryEntity productExpiredRepositoryEntity) {
        this.productExpiredRepositoryEntity = productExpiredRepositoryEntity;
    }

    @KafkaListener(
            topics = "product-delete",
            groupId = "product-delete-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void productDelete(ProductDeleteEvent event){
        productExpiredRepositoryEntity.deleteById(event.idProduct());
    }
}
