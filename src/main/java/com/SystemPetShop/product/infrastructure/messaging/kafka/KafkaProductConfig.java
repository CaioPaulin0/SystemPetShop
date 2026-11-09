package com.SystemPetShop.product.infrastructure.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaProductConfig {

    @Bean
    public NewTopic productCreateTopicMetrics() {
        return TopicBuilder.name("product-created-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productUpdateTopicMetrics() {
        return TopicBuilder.name("product-update-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productDeleteTopicMetrics() {
        return TopicBuilder.name("product-delete-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productNewVersionTopicMetrics() {
        return TopicBuilder.name("product-new-version-metrics")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productCreateTopic(){
        return TopicBuilder.name("product-created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productDeleteTopic(){
        return TopicBuilder.name("product-delete")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productNewVersionTopic(){
        return TopicBuilder.name("product-new-version")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productUpdateTopic(){
        return TopicBuilder.name("product-update")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
