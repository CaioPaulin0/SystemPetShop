package com.SystemPetShop.expired.infrastructure.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaExpiredConfig {
    @Bean
    public NewTopic expiredVerifyTopic(){
        return TopicBuilder.name("expired-verify")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
