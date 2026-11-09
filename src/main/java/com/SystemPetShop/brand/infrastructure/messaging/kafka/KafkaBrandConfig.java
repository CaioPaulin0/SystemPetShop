package com.SystemPetShop.brand.infrastructure.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaBrandConfig {

    @Bean
    public NewTopic brandCreateTopicMetrics() {
        return TopicBuilder.name("brand-created-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic brandUpdateTopicMetrics() {
        return TopicBuilder.name("brand-update-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic brandDeleteTopicMetrics() {
        return TopicBuilder.name("brand-delete-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic brandCreateTopic(){
        return TopicBuilder.name("brand-created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic brandDeleteTopic(){
        return TopicBuilder.name("brand-delete")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic brandUpdateTopic(){
        return TopicBuilder.name("brand-update")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
