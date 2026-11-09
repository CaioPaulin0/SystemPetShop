package com.SystemPetShop;

import com.SystemPetShop.brand.application.*;
import com.SystemPetShop.brand.application.input.*;
import com.SystemPetShop.brand.infrastructure.repository.JpaBrandRepository;
import com.SystemPetShop.distributor.application.*;
import com.SystemPetShop.distributor.application.input.DistributorInput;
import com.SystemPetShop.distributor.infrastructure.repository.JpaDistributorRepository;
import com.SystemPetShop.expired.application.ExpiredVerifyUseCase;
import com.SystemPetShop.expired.infrastructure.repository.JpaExpiredEntityRepository;
import com.SystemPetShop.product.application.*;
import com.SystemPetShop.product.application.input.*;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import com.SystemPetShop.product.infrastructure.messaging.kafka.repository.ProductExpiredRepositoryEntity;
import com.SystemPetShop.product.infrastructure.repository.JpaProductEntityRepository;
import com.SystemPetShop.product.infrastructure.repository.ProductEntityRepository;
import com.SystemPetShop.shared.infrastructure.outbox.*;
import com.SystemPetShop.shared.infrastructure.persistence.config.*;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OutboxUseCaseTransactionTest {
    AnnotationConfigApplicationContext context;
    JdbcTemplate main;
    JdbcTemplate expired;
    final UUID brandId = UUID.randomUUID();
    final UUID distributorId = UUID.randomUUID();

    @BeforeEach void start() {
        context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().addBeanPostProcessor(new BeanPostProcessor() {
            @Override public Object postProcessBeforeInitialization(Object bean, String name) {
                if (bean instanceof HikariDataSource ds) {
                    ds.setDriverClassName("org.h2.Driver");
                    ds.setJdbcUrl("jdbc:h2:mem:" + name + UUID.randomUUID() + ";MODE=MySQL");
                    ds.setUsername("sa");
                    ds.setPassword("");
                }
                if (bean instanceof LocalContainerEntityManagerFactoryBean factory) {
                    factory.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
                }
                return bean;
            }
        });
        context.register(TestConfig.class);
        context.refresh();
        for (String name : List.of("mainDataSource", "expiredDataSource")) {
            new ResourceDatabasePopulator(new ClassPathResource("db/manual/2026-11-04-outbox.sql"))
                    .execute(context.getBean(name, DataSource.class));
        }
        main = new JdbcTemplate(context.getBean("mainDataSource", DataSource.class));
        expired = new JdbcTemplate(context.getBean("expiredDataSource", DataSource.class));
    }
    @AfterEach void stop() { if (context != null) context.close(); }

    @Test void crudGravaEventosNoPrincipalSemEnviarParaCopias() throws Exception {
        var distributor = context.getBean(DistributorSaveUseCase.class).execute(new DistributorInput(
                "Distribuidor", "Centro", List.of(), "Atacado", List.of(), "123"));
        var distributorUuid = UUID.fromString(distributor.id());
        var brand = context.getBean(BrandSaveUseCase.class).execute(new BrandInput(distributorUuid, "Marca", "Racao"));
        var product = context.getBean(ProductSaveUseCase.class).execute(input("Produto", brand.idBrand(), distributorUuid));
        assertThat(pending(main)).isEqualTo(6);
        assertThat(pending(expired)).isZero();
        assertThat(expired.queryForObject("SELECT COUNT(*) FROM product_entity", Integer.class)).isZero();

        context.getBean(BrandEditUseCase.class).execute(new BrandEditInput(brand.idBrand(), distributorUuid, "Marca editada", "Racao"));
        var date = LocalDate.of(2026, 11, 4);
        context.getBean(ProductEditUseCase.class).execute(new ProductEditInput(product.idProduct(), brand.idBrand(), distributorUuid,
                "Racao", "Editado", date, date.plusYears(1), "001-A", 12, 24, 10));
        context.getBean(ProductDeleteUseCase.class).execute(product.idProduct());
        context.getBean(BrandDeleteUseCase.class).execute(brand.idBrand());
        context.getBean(DistributorDeleteUseCase.class).execute(distributorUuid);
        assertThat(pending(main)).isEqualTo(16);
        assertThat(main.queryForObject("SELECT COUNT(*) FROM product_entity", Integer.class)).isZero();
        assertThat(main.queryForObject("SELECT COUNT(*) FROM distributor_entity", Integer.class)).isZero();
        assertThat(main.queryForList("SELECT topic FROM outbox_event", String.class))
                .contains("product-created", "product-created-metrics", "product-update", "product-update-metrics",
                        "product-delete", "product-delete-metrics", "brand-created", "brand-created-metrics",
                        "brand-update", "brand-update-metrics", "brand-delete", "brand-delete-metrics",
                        "distributor-created", "distributor-created-metrics", "distributor-delete", "distributor-delete-metrics");
    }

    @Test void falhaNaOutboxReverteCadastroJpa() {
        main.execute("DROP TABLE outbox_event");
        assertThatThrownBy(() -> context.getBean(ProductSaveUseCase.class).execute(input("Produto", brandId, distributorId)))
                .isInstanceOf(RuntimeException.class);
        assertThat(main.queryForObject("SELECT COUNT(*) FROM product_entity", Integer.class)).isZero();
    }

    @Test void falhaJpaNoCommitNaoDeixaEventosDaOperacaoInvalida() {
        var save = context.getBean(ProductSaveUseCase.class);
        save.execute(input("Mesmo nome", brandId, distributorId));
        assertThatThrownBy(() -> save.execute(input("Mesmo nome", brandId, distributorId)))
                .isInstanceOf(RuntimeException.class);
        assertThat(pending(main)).isEqualTo(2);
        assertThat(main.queryForObject("SELECT COUNT(*) FROM product_entity", Integer.class)).isEqualTo(1);
    }

    @Test void falhaNaNovaVersaoReverteDesativacaoEEventos() {
        var product = context.getBean(ProductSaveUseCase.class).execute(input("Original", brandId, distributorId));
        var useCase = context.getBean(ProductNewVersionUseCase.class);
        assertThatThrownBy(() -> useCase.execute(version(product.idProduct(), "Original")))
                .isInstanceOf(RuntimeException.class);
        assertThat(context.getBean(ProductEntityRepository.class).findById(product.idProduct()).orElseThrow().isProductEnableDue()).isTrue();
        assertThat(pending(main)).isEqualTo(2);
    }

    @Test void verificacaoDeValidadeUsaOutboxExpiredEReverteJuntoComFlag() {
        var date = LocalDate.of(2020, 1, 1);
        var product = new Product(brandId, distributorId, "Vencido", "Racao", "Descricao", date,
                date.plusYears(1), "001-A", 10, 20, 5, false, 1, true);
        var repo = context.getBean(ProductExpiredRepositoryEntity.class);
        repo.save(ProductEntity.from(product));
        var id = product.getIdProduct().id();
        var tx = new TransactionTemplate(context.getBean("expiredTransactionManager", PlatformTransactionManager.class));
        tx.executeWithoutResult(status -> {
            context.getBean(ExpiredVerifyUseCase.class).execute(id);
            assertThat(pending(expired)).isEqualTo(1);
            status.setRollbackOnly();
        });
        assertThat(repo.findById(id).orElseThrow().isDataDueProductVerify()).isFalse();
        assertThat(pending(expired)).isZero();
        context.getBean(ExpiredVerifyUseCase.class).execute(id);
        assertThat(repo.findById(id).orElseThrow().isDataDueProductVerify()).isTrue();
        assertThat(pending(expired)).isEqualTo(1);
        assertThat(pending(main)).isZero();
        assertThat(expired.queryForObject("SELECT topic FROM outbox_event", String.class)).isEqualTo("expired-verify");
    }

    @Test void transacaoPrincipalNaoAutorizaEscritaNaOutboxExpired() {
        var tx = new TransactionTemplate(context.getBean("mainTransactionManager", PlatformTransactionManager.class));
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> context.getBean("expiredOutbox", OutboxStore.class)
                .enqueue("expired-verify", "id", "event"))).isInstanceOf(IllegalTransactionStateException.class);
        assertThat(pending(main)).isZero();
        assertThat(pending(expired)).isZero();
    }

    private int pending(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM outbox_event WHERE published_at IS NULL", Integer.class);
    }
    private ProductInput input(String name, UUID brand, UUID distributor) {
        var date = LocalDate.of(2026, 11, 4);
        return new ProductInput(brand, distributor, name, "Racao", "Descricao", date,
                date.plusYears(1), "001-A", 10, 20, 5, false, 1);
    }
    private ProductNewVersionInput version(UUID previous, String name) {
        var data = input(name, brandId, distributorId);
        return new ProductNewVersionInput(previous, brandId, distributorId, name, data.typeProduct(), data.descriptionProduct(),
                data.dataCreatedProduct(), data.dataDueProduct(), data.lotProduct(), 10, 20, 5, false, 1);
    }

    @Configuration
    @Import({MainDbConfig.class, ExpiredDbConfig.class, MetricsDbConfig.class, OutboxConfig.class,
            JpaProductEntityRepository.class, JpaBrandRepository.class, JpaDistributorRepository.class, JpaExpiredEntityRepository.class,
            ProductSaveUseCase.class, ProductEditUseCase.class, ProductDeleteUseCase.class, ProductNewVersionUseCase.class,
            BrandSaveUseCase.class, BrandEditUseCase.class, BrandDeleteUseCase.class,
            DistributorSaveUseCase.class, DistributorDeleteUseCase.class, ExpiredVerifyUseCase.class})
    @ComponentScan(basePackages = {"com.SystemPetShop.product.infrastructure.messaging.kafka.produce",
            "com.SystemPetShop.brand.infrastructure.messaging.kafka.produce",
            "com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce",
            "com.SystemPetShop.expired.infrastructure.messaging.produce"})
    static class TestConfig {}
}
