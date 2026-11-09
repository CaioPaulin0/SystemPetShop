package com.SystemPetShop;

import com.SystemPetShop.brand.infrastructure.messaging.kafka.KafkaBrandConfig;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.consume.BrandCreateMetricsConsume;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.consume.BrandCreatedConsume;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.consume.BrandDeleteConsume;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.consume.BrandDeleteMetricsConsume;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.consume.BrandEditConsume;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.consume.BrandEditMetricsConsume;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandDeleteEvent;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.repository.BrandExpiredRepositoryEntity;
import com.SystemPetShop.brand.infrastructure.repository.BrandEntityRepository;
import com.SystemPetShop.brand.infrastructure.metrics.BrandMetricsRepository;
import com.SystemPetShop.shared.infrastructure.persistence.config.ExpiredDbConfig;
import com.SystemPetShop.shared.infrastructure.persistence.config.MainDbConfig;
import com.SystemPetShop.shared.infrastructure.persistence.config.MetricsDbConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BrandConsumePersistenceTest {

    @Test
    void deveRegistrarListenersEPersistirComRollbackSomenteNoBancoCorrespondente() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.setAllowBeanDefinitionOverriding(false);
            context.getBeanFactory().addBeanPostProcessor(new BeanPostProcessor() {
                @Override
                public Object postProcessBeforeInitialization(Object bean, String beanName) {
                    if (bean instanceof HikariDataSource dataSource) {
                        dataSource.setDriverClassName("org.h2.Driver");
                        dataSource.setJdbcUrl("jdbc:h2:mem:" + beanName + UUID.randomUUID());
                        dataSource.setUsername("sa");
                        dataSource.setPassword("");
                    }
                    if (bean instanceof LocalContainerEntityManagerFactoryBean factory) {
                        factory.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
                    }
                    return bean;
                }
            });
            context.register(MainDbConfig.class, ExpiredDbConfig.class, MetricsDbConfig.class,
                    KafkaBrandConfig.class, ListenerConfig.class,
                    BrandCreatedConsume.class, BrandEditConsume.class, BrandDeleteConsume.class,
                    BrandCreateMetricsConsume.class, BrandEditMetricsConsume.class, BrandDeleteMetricsConsume.class);
            context.refresh();

            var topics = context.getBeansOfType(NewTopic.class).values().stream().map(NewTopic::name).toList();
            assertThat(topics).containsExactlyInAnyOrder("brand-created", "brand-update", "brand-delete",
                    "brand-created-metrics", "brand-update-metrics", "brand-delete-metrics");
            var listeners = context.getBean(KafkaListenerEndpointRegistry.class).getListenerContainers();
            assertThat(listeners).hasSize(6).allSatisfy(listener -> assertThat(listener.isRunning()).isFalse());
            assertThat(listeners.stream().flatMap(listener -> Arrays.stream(
                    listener.getContainerProperties().getTopics())).toList())
                    .containsExactlyInAnyOrderElementsOf(topics);

            var create = context.getBean(BrandCreateMetricsConsume.class);
            var edit = context.getBean(BrandEditMetricsConsume.class);
            var delete = context.getBean(BrandDeleteMetricsConsume.class);
            var main = context.getBean(BrandEntityRepository.class);
            var expired = context.getBean(BrandExpiredRepositoryEntity.class);
            var metrics = context.getBean(BrandMetricsRepository.class);
            var transaction = new TransactionTemplate(context.getBean(
                    "metricsTransactionManager", PlatformTransactionManager.class));
            var event = new BrandCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "Original", "Racao");
            var updated = new BrandCreatedEvent(event.idBrand(), event.idDistributor(), "Editada", "Higiene");
            var deletion = new BrandDeleteEvent(event.idBrand());

            main.save(BrandCreatedEvent.toEntity(event));
            context.getBean(BrandCreatedConsume.class).createBrand(event);
            assertThat(metrics.findById(event.idBrand())).isEmpty();

            transaction.executeWithoutResult(status -> {
                create.createBrandMetrics(event);
                metrics.flush();
                status.setRollbackOnly();
            });
            assertThat(metrics.findById(event.idBrand())).isEmpty();

            create.createBrandMetrics(event);
            create.createBrandMetrics(event);
            assertThat(metrics.count()).isEqualTo(1);
            assertThat(metrics.findById(event.idBrand())).get()
                    .usingRecursiveComparison().isEqualTo(BrandCreatedEvent.toEntity(event));

            transaction.executeWithoutResult(status -> {
                edit.editBrandMetrics(updated);
                metrics.flush();
                status.setRollbackOnly();
            });
            assertThat(metrics.findById(event.idBrand())).get()
                    .usingRecursiveComparison().isEqualTo(BrandCreatedEvent.toEntity(event));

            edit.editBrandMetrics(updated);
            assertThat(metrics.findById(event.idBrand())).get()
                    .usingRecursiveComparison().isEqualTo(BrandCreatedEvent.toEntity(updated));

            transaction.executeWithoutResult(status -> {
                delete.deleteBrandMetrics(deletion);
                metrics.flush();
                status.setRollbackOnly();
            });
            assertThat(metrics.findById(event.idBrand())).isPresent();

            delete.deleteBrandMetrics(deletion);
            delete.deleteBrandMetrics(deletion);
            assertThat(metrics.findById(event.idBrand())).isEmpty();
            assertThat(main.findById(event.idBrand())).get()
                    .usingRecursiveComparison().isEqualTo(BrandCreatedEvent.toEntity(event));
            assertThat(expired.findById(event.idBrand())).get()
                    .usingRecursiveComparison().isEqualTo(BrandCreatedEvent.toEntity(event));
        }
    }

    @Configuration
    @EnableKafka
    static class ListenerConfig {
        @Bean
        ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory() {
            var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
            factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                    Map.of("bootstrap.servers", "localhost:9092")));
            // Register real listeners without requiring or contacting an external broker.
            factory.setAutoStartup(false);
            return factory;
        }
    }
}
