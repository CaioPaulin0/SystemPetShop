package com.SystemPetShop.expired.infrastructure.messaging.consume;

import com.SystemPetShop.expired.infrastructure.messaging.dto.ExpiredVerifyEvent;
import com.SystemPetShop.expired.infrastructure.repository.ExpiredEntityRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpiredVerifyConsume {
    private final ExpiredEntityRepository expiredEntityRepository;

    public ExpiredVerifyConsume(ExpiredEntityRepository expiredEntityRepository) {
        this.expiredEntityRepository = expiredEntityRepository;
    }

    @KafkaListener(
            topics = "expired-verify",
            groupId = "expired-verify-1",
            properties = {
                    "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "value.deserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer",
                    "spring.json.value.default.type=com.SystemPetShop.expired.infrastructure.messaging.dto.ExpiredVerifyEvent",
                    "spring.json.use.type.headers=false"
            }
    )
    @Transactional(transactionManager = "expiredTransactionManager")
    public void verifyExpired(ExpiredVerifyEvent event){
        expiredEntityRepository.verifyDueProduct(event.idProduct());
    }
}
