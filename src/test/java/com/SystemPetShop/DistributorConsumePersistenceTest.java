package com.SystemPetShop;

import com.SystemPetShop.brand.domain.BrandRepository;
import com.SystemPetShop.brand.infrastructure.repository.JpaBrandRepository;
import com.SystemPetShop.distributor.domain.DistributorRepository;
import com.SystemPetShop.distributor.infrastructure.repository.JpaDistributorRepository;
import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.consume.DistributorConsume;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.consume.DistributorDeleteConsume;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.dto.DistributorDeleteEvent;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent;
import com.SystemPetShop.distributor.infrastructure.repository.DistributorRepositoryEntity;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.repository.DistributorExpiredRepositoryEntity;
import com.SystemPetShop.shared.infrastructure.persistence.config.ExpiredDbConfig;
import com.SystemPetShop.shared.infrastructure.persistence.config.MainDbConfig;
import com.SystemPetShop.shared.infrastructure.persistence.config.MetricsDbConfig;
import com.SystemPetShop.distributor.infrastructure.metrics.DistributorMetricsRepository;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import com.SystemPetShop.product.infrastructure.repository.ProductEntityRepository;
import com.SystemPetShop.product.infrastructure.repository.JpaProductEntityRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.repository.ProductExpiredRepositoryEntity;
import com.SystemPetShop.product.domain.ProductRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DistributorConsumePersistenceTest {

    @Test
    void deveSalvarSomenteNoExpiredEParticiparDaTransacaoExpired() {
        try (var context = new AnnotationConfigApplicationContext()) {
            // Exercise the production repository registrations with three isolated databases.
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
                    DistributorConsume.class, DistributorDeleteConsume.class, JpaProductEntityRepository.class,
                    JpaDistributorRepository.class, JpaBrandRepository.class);
            context.refresh();

            var consumer = context.getBean(DistributorConsume.class);
            var mainRepository = context.getBean(DistributorRepositoryEntity.class);
            var expiredRepository = context.getBean(DistributorExpiredRepositoryEntity.class);
            var metricsRepository = context.getBean(DistributorMetricsRepository.class);
            var event = event("Distribuidor confirmado");

            consumer.receiveDistributor(event);

            assertThat(expiredRepository.findById(event.idDistributor())).isPresent();
            assertThat(mainRepository.findById(event.idDistributor())).isEmpty();
            assertThat(metricsRepository.findById(event.idDistributor())).isEmpty();

            var rolledBackEvent = event("Distribuidor revertido");
            var transaction = new TransactionTemplate(context.getBean(
                    "expiredTransactionManager", PlatformTransactionManager.class));
            transaction.executeWithoutResult(status -> {
                consumer.receiveDistributor(rolledBackEvent);
                expiredRepository.flush();
                status.setRollbackOnly();
            });

            assertThat(expiredRepository.findById(rolledBackEvent.idDistributor())).isEmpty();
            assertThat(mainRepository.findById(rolledBackEvent.idDistributor())).isEmpty();
            assertThat(metricsRepository.findById(rolledBackEvent.idDistributor())).isEmpty();

            var metricsEvent = event("Distribuidor de metricas");
            consumer.receiveDistributorMetrics(metricsEvent);
            assertThat(metricsRepository.findById(metricsEvent.idDistributor())).isPresent();
            assertThat(mainRepository.findById(metricsEvent.idDistributor())).isEmpty();
            assertThat(expiredRepository.findById(metricsEvent.idDistributor())).isEmpty();

            var rolledBackMetricsEvent = event("Metricas revertidas");
            var metricsTransaction = new TransactionTemplate(context.getBean(
                    "metricsTransactionManager", PlatformTransactionManager.class));
            metricsTransaction.executeWithoutResult(status -> {
                consumer.receiveDistributorMetrics(rolledBackMetricsEvent);
                metricsRepository.flush();
                status.setRollbackOnly();
            });
            assertThat(metricsRepository.findById(rolledBackMetricsEvent.idDistributor())).isEmpty();
            assertThat(mainRepository.findById(rolledBackMetricsEvent.idDistributor())).isEmpty();
            assertThat(expiredRepository.findById(rolledBackMetricsEvent.idDistributor())).isEmpty();

            verifyMetricsQueries(context);
            verifyDistributorDeletes(context);
        }
    }

    private void verifyDistributorDeletes(AnnotationConfigApplicationContext context) {
        var consumer = context.getBean(DistributorDeleteConsume.class);
        var mainRepository = context.getBean(DistributorRepositoryEntity.class);
        var expiredRepository = context.getBean(DistributorExpiredRepositoryEntity.class);
        var metricsRepository = context.getBean(DistributorMetricsRepository.class);
        var event = event("Distribuidor para excluir");
        var entity = new DistributorEntity(
                event.idDistributor(), event.nameDistributor(), event.localizationDistributor(),
                event.salespeopleDistributor(), event.typeDistributor(), event.telephones(), event.CNPJ());
        mainRepository.save(entity);
        expiredRepository.save(entity);
        metricsRepository.save(entity);
        var deleteEvent = new DistributorDeleteEvent(event.idDistributor());

        var transaction = new TransactionTemplate(context.getBean(
                "metricsTransactionManager", PlatformTransactionManager.class));
        transaction.executeWithoutResult(status -> {
            consumer.deleteDistributorMetrics(deleteEvent);
            metricsRepository.flush();
            assertThat(metricsRepository.findById(event.idDistributor())).isEmpty();
            status.setRollbackOnly();
        });
        assertThat(metricsRepository.findById(event.idDistributor())).isPresent();
        assertThat(expiredRepository.findById(event.idDistributor())).isPresent();
        assertThat(mainRepository.findById(event.idDistributor())).isPresent();

        consumer.deleteDistributorMetrics(deleteEvent);
        assertThat(metricsRepository.findById(event.idDistributor())).isEmpty();
        assertThat(expiredRepository.findById(event.idDistributor())).isPresent();
        assertThat(mainRepository.findById(event.idDistributor())).isPresent();

        // Redelivery of an already applied delete must also succeed.
        consumer.deleteDistributorMetrics(deleteEvent);
        assertThat(expiredRepository.findById(event.idDistributor())).isPresent();
        assertThat(mainRepository.findById(event.idDistributor())).isPresent();

        metricsRepository.save(entity);
        consumer.deleteDistributor(deleteEvent);
        assertThat(expiredRepository.findById(event.idDistributor())).isEmpty();
        assertThat(metricsRepository.findById(event.idDistributor())).isPresent();
        assertThat(mainRepository.findById(event.idDistributor())).isPresent();
    }

    private void verifyMetricsQueries(AnnotationConfigApplicationContext context) {
        var brandId = UUID.randomUUID();
        var distributorId = UUID.randomUUID();
        var metricsProducts = context.getBean(ProductMetricsRepository.class);
        var brandRepository = context.getBean(BrandRepository.class);
        var distributorRepository = context.getBean(DistributorRepository.class);
        var productRepository = context.getBean(ProductRepository.class);

        // Different values in each database expose accidental reads from main or expired.
        context.getBean(ProductEntityRepository.class).save(product(brandId, distributorId, 100, 200, true));
        context.getBean(ProductExpiredRepositoryEntity.class).save(product(brandId, distributorId, 300, 400, true));
        var active = metricsProducts.save(product(brandId, distributorId, 10, 20, true));
        var inactive = metricsProducts.save(product(brandId, distributorId, 50, 60, false));
        metricsProducts.save(product(UUID.randomUUID(), UUID.randomUUID(), 500, 600, true));

        assertThat(productRepository.metricsProduct(brandId, PageRequest.of(0, 12)))
                .extracting(product -> product.getIdProduct().id())
                .containsExactlyInAnyOrder(active.getIdProduct(), inactive.getIdProduct());
        assertThat(brandRepository.metricsBrand(brandId)).containsExactly(10.0, 20.0);
        assertThat(distributorRepository.metricsDistributor(distributorId)).containsExactly(10.0, 20.0);
        assertThat(productRepository.metricsProduct(UUID.randomUUID(), PageRequest.of(0, 12))).isEmpty();
        assertThat(brandRepository.metricsBrand(UUID.randomUUID())).containsExactly(0.0, 0.0);
        assertThat(distributorRepository.metricsDistributor(UUID.randomUUID())).containsExactly(0.0, 0.0);

        var paginatedBrandId = UUID.randomUUID();
        for (int i = 0; i < 13; i++) {
            metricsProducts.save(product(paginatedBrandId, distributorId, 10, 20, true));
        }
        var firstPage = productRepository.metricsProduct(paginatedBrandId, PageRequest.of(0, 12));
        var secondPage = productRepository.metricsProduct(paginatedBrandId, PageRequest.of(1, 12));
        assertThat(firstPage).hasSize(12);
        assertThat(secondPage).hasSize(1);
        assertThat(firstPage).extracting(product -> product.getIdProduct().id())
                .doesNotContain(secondPage.getFirst().getIdProduct().id());
        assertThat(productRepository.metricsProduct(paginatedBrandId, PageRequest.of(2, 12))).isEmpty();
    }

    private ProductEntity product(UUID brandId, UUID distributorId, double buy, double sell, boolean active) {
        var id = UUID.randomUUID();
        var date = LocalDate.of(2026, 11, 3);
        return new ProductEntity(id, brandId, distributorId, "Produto " + id, "Racao", "Teste",
                date, date, date.plusMonths(1), "Lote " + id, buy, sell, 1, false, 1, active);
    }

    private DistributorCreatedEvent event(String name) {
        return new DistributorCreatedEvent(UUID.randomUUID(), name, "Centro",
                List.of("Ana"), "Atacado", List.of("11999990000"), "12345678000190");
    }
}
