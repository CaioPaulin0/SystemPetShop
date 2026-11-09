package com.SystemPetShop;

import com.SystemPetShop.brand.infrastructure.metrics.BrandMetricsRepository;
import com.SystemPetShop.distributor.infrastructure.metrics.DistributorMetricsRepository;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import com.SystemPetShop.product.infrastructure.messaging.kafka.KafkaProductConfig;
import com.SystemPetShop.product.infrastructure.messaging.kafka.consume.*;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionMetricsEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.repository.ProductExpiredRepositoryEntity;
import com.SystemPetShop.product.infrastructure.repository.ProductEntityRepository;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProductMetricsConsumePersistenceTest {

    @Test
    void deveIsolarMetricsEPreservarTransacoesDeCriacaoEdicaoExclusaoENovaVersao() {
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
                    KafkaProductConfig.class, ListenerConfig.class,
                    ProductCreatedConsume.class, ProductEditConsume.class, ProductDeleteConsume.class,
                    ProductNewVersionConsume.class, ProductCreatedMetricsConsume.class,
                    ProductEditMetricsConsume.class, ProductDeleteMetricsConsume.class, ProductNewVersionMetricsConsume.class);
            context.refresh();

            var topics = context.getBeansOfType(NewTopic.class).values().stream().map(NewTopic::name).toList();
            assertThat(topics).containsExactlyInAnyOrder(
                    "product-created", "product-update", "product-delete", "product-new-version",
                    "product-created-metrics", "product-update-metrics", "product-delete-metrics", "product-new-version-metrics");
            var listeners = context.getBean(KafkaListenerEndpointRegistry.class).getListenerContainers();
            assertThat(listeners).hasSize(8).allSatisfy(listener -> assertThat(listener.isRunning()).isFalse());
            assertThat(listeners.stream().flatMap(listener -> Arrays.stream(
                    listener.getContainerProperties().getTopics())).toList()).containsExactlyInAnyOrderElementsOf(topics);

            var create = context.getBean(ProductCreatedMetricsConsume.class);
            var edit = context.getBean(ProductEditMetricsConsume.class);
            var delete = context.getBean(ProductDeleteMetricsConsume.class);
            var newVersion = context.getBean(ProductNewVersionMetricsConsume.class);
            var main = context.getBean(ProductEntityRepository.class);
            var expired = context.getBean(ProductExpiredRepositoryEntity.class);
            var metrics = context.getBean(ProductMetricsRepository.class);
            var brandMetrics = context.getBean(BrandMetricsRepository.class);
            var transaction = new TransactionTemplate(context.getBean(
                    "metricsTransactionManager", PlatformTransactionManager.class));
            var product = product("Original", UUID.randomUUID(), UUID.randomUUID(), 10, 20);
            var original = ProductCreatedEvent.toEvent(product);
            var id = original.idProduct();

            main.save(ProductEntity.from(product));
            context.getBean(ProductCreatedConsume.class).createProduct(original);
            assertThat(metrics.findById(id)).isEmpty();

            transaction.executeWithoutResult(status -> {
                create.createProductMetrics(original);
                metrics.flush();
                status.setRollbackOnly();
            });
            assertThat(metrics.findById(id)).isEmpty();

            create.createProductMetrics(original);
            create.createProductMetrics(original);
            assertThat(metrics.count()).isEqualTo(1);
            assertThat(metrics.findById(id)).get().usingRecursiveComparison()
                    .isEqualTo(ProductCreatedEvent.toEntity(original));

            product.setPriceBuy(25);
            product.setPriceSell(40);
            product.setLotProduct("00AB-002");
            product.setDescriptionProduct("Editado");
            var updated = ProductCreatedEvent.toEvent(product);
            transaction.executeWithoutResult(status -> {
                edit.editProductMetrics(updated);
                status.setRollbackOnly();
            });
            assertThat(metrics.findById(id)).get().usingRecursiveComparison()
                    .isEqualTo(ProductCreatedEvent.toEntity(original));

            edit.editProductMetrics(updated);
            assertThat(metrics.findById(id)).get().usingRecursiveComparison()
                    .isEqualTo(ProductCreatedEvent.toEntity(updated));
            var absent = ProductCreatedEvent.toEvent(product("Ausente", original.idBrand(), original.idDistributor(), 1, 2));
            assertThatThrownBy(() -> edit.editProductMetrics(absent)).isInstanceOf(IllegalStateException.class);

            var next = product("Nova versao", original.idBrand(), original.idDistributor(), 30, 50);
            next.setVersionProduct(2);
            var versionEvent = ProductNewVersionMetricsEvent.toEvent(id, next);
            var nextId = next.getIdProduct().id();
            // Both the previous version's flag and the new row must roll back together.
            transaction.executeWithoutResult(status -> {
                newVersion.newVersionProductMetrics(versionEvent);
                metrics.flush();
                status.setRollbackOnly();
            });
            assertThat(metrics.findById(nextId)).isEmpty();
            assertThat(metrics.findById(id).orElseThrow().isProductEnableDue()).isTrue();

            next.setNameProduct(null);
            assertThatThrownBy(() -> newVersion.newVersionProductMetrics(
                    ProductNewVersionMetricsEvent.toEvent(id, next))).isInstanceOf(RuntimeException.class);
            assertThat(metrics.findById(nextId)).isEmpty();
            assertThat(metrics.findById(id).orElseThrow().isProductEnableDue()).isTrue();
            next.setNameProduct("Nova versao");

            assertThatThrownBy(() -> newVersion.newVersionProductMetrics(
                    ProductNewVersionMetricsEvent.toEvent(UUID.randomUUID(), next)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(metrics.findById(nextId)).isEmpty();

            newVersion.newVersionProductMetrics(versionEvent);
            newVersion.newVersionProductMetrics(versionEvent);
            assertThat(metrics.count()).isEqualTo(2);
            assertThat(metrics.findById(id).orElseThrow().isProductEnableDue()).isFalse();
            assertThat(metrics.findById(nextId)).get().usingRecursiveComparison().isEqualTo(ProductEntity.from(next));
            assertThat(brandMetrics.metricsBrand(original.idBrand()).totalBuy()).isEqualTo(30);
            assertThat(brandMetrics.metricsBrand(original.idBrand()).totalSell()).isEqualTo(50);
            var totals = context.getBean(DistributorMetricsRepository.class).metricsDistributor(original.idDistributor());
            assertThat(totals.totalBuy()).isEqualTo(30);
            assertThat(totals.totalSell()).isEqualTo(50);

            // Replayed edits must not reactivate a historical version.
            edit.editProductMetrics(updated);
            assertThat(metrics.findById(id).orElseThrow().isProductEnableDue()).isFalse();

            var deletion = new ProductDeleteEvent(nextId);
            transaction.executeWithoutResult(status -> {
                delete.deleteProductMetrics(deletion);
                metrics.flush();
                status.setRollbackOnly();
            });
            assertThat(metrics.findById(nextId)).isPresent();
            delete.deleteProductMetrics(deletion);
            delete.deleteProductMetrics(deletion);
            assertThat(metrics.findById(nextId)).isEmpty();
            assertThat(brandMetrics.metricsBrand(original.idBrand()).totalBuy()).isZero();
            assertThat(brandMetrics.metricsBrand(original.idBrand()).totalSell()).isZero();

            assertThat(main.findById(id)).get().usingRecursiveComparison()
                    .isEqualTo(ProductCreatedEvent.toEntity(original));
            assertThat(expired.findById(id)).get().usingRecursiveComparison()
                    .isEqualTo(ProductCreatedEvent.toEntity(original));
            assertThat(main.findById(nextId)).isEmpty();
            assertThat(expired.findById(nextId)).isEmpty();
        }
    }

    private Product product(String name, UUID brandId, UUID distributorId, double buy, double sell) {
        var date = LocalDate.of(2026, 11, 3);
        return new Product(brandId, distributorId, name, "Alimento", "Descricao",
                date, date.plusYears(1), "00123", buy, sell, 5, false, 1, true);
    }

    @Configuration
    @EnableKafka
    static class ListenerConfig {
        @Bean
        ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory() {
            var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
            factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                    Map.of("bootstrap.servers", "localhost:9092")));
            factory.setAutoStartup(false);
            return factory;
        }
    }
}
