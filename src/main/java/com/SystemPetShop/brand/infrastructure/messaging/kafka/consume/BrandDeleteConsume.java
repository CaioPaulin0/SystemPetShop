package com.SystemPetShop.brand.infrastructure.messaging.kafka.consume;

import com.SystemPetShop.brand.infrastructure.messaging.kafka.repository.BrandExpiredRepositoryEntity;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandDeleteEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandDeleteConsume {
    private final BrandExpiredRepositoryEntity brandExpiredRepositoryEntity;

    public BrandDeleteConsume(BrandExpiredRepositoryEntity brandExpiredRepositoryEntity) {
        this.brandExpiredRepositoryEntity = brandExpiredRepositoryEntity;
    }

    @KafkaListener(
            topics = "brand-delete",
            groupId = "brand-delete-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandDeleteEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void brandDelete(BrandDeleteEvent event){
        brandExpiredRepositoryEntity.deleteById(event.idBrand());
    }
}
