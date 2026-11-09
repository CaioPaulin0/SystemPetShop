package com.SystemPetShop.product.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.product.infrastructure.messaging.kafka.repository.ProductExpiredRepositoryEntity;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductNewVersionConsume {
    private final ProductExpiredRepositoryEntity productExpiredRepositoryEntity;

    public ProductNewVersionConsume(ProductExpiredRepositoryEntity productExpiredRepositoryEntity) {
        this.productExpiredRepositoryEntity = productExpiredRepositoryEntity;
    }

    @KafkaListener(
            topics = "product-new-version",
            groupId = "product-new-version-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void newVersionProduct(ProductNewVersionEvent event){
        productExpiredRepositoryEntity.save(ProductNewVersionEvent.toEntity(event));
    }
}
