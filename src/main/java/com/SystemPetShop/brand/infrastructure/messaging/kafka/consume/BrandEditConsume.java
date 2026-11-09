package com.SystemPetShop.brand.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.brand.infrastructure.messaging.kafka.repository.BrandExpiredRepositoryEntity;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandEditConsume {
    private final BrandExpiredRepositoryEntity brandExpiredRepositoryEntity;

    public BrandEditConsume(BrandExpiredRepositoryEntity brandExpiredRepositoryEntity) {
        this.brandExpiredRepositoryEntity = brandExpiredRepositoryEntity;
    }

    @KafkaListener(
            topics = "brand-update",
            groupId = "brand-update-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void brandEdit(BrandCreatedEvent event){
        brandExpiredRepositoryEntity.editBrand(BrandCreatedEvent.toEntity(event));
    }
}
