package com.SystemPetShop.distributor.infrastructure.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


// configurar para receber da forma correta //
// https://chatgpt.com/c/6ad74129-d9cc-83e9-ad0f-04110b00ded6 //
@Configuration
public class KafkaDistributorConfig {

    @Bean
    public NewTopic distributorCreateTopic(){
        return TopicBuilder.name("distributor-created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic distributorDeleteTopic(){
        return TopicBuilder.name("distributor-delete")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic distributorCreateTopicMetrics(){
        return TopicBuilder.name("distributor-created-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic distributorDeleteTopicMetrics(){
        return TopicBuilder.name("distributor-delete-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
