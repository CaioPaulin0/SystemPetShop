package com.SystemPetShop;

import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;
import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.expired.application.ExpiredFindAllUseCase;
import com.SystemPetShop.expired.infrastructure.entity.ExpiredEntity;
import com.SystemPetShop.expired.infrastructure.repository.ExpiredEntityRepository;
import com.SystemPetShop.expired.infrastructure.repository.JpaExpiredEntityRepository;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpiredRepositoryTest {
    private SessionFactory sessionFactory;
    private Session session;
    private ExpiredEntityRepository queryRepository;
    private JpaExpiredEntityRepository repository;

    @BeforeEach
    void setUp() {
        sessionFactory = new Configuration()
                .addAnnotatedClass(ProductEntity.class)
                .addAnnotatedClass(ExpiredEntity.class)
                .addAnnotatedClass(BrandEntity.class)
                .addAnnotatedClass(DistributorEntity.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:expired_" + UUID.randomUUID())
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .buildSessionFactory();
        session = sessionFactory.openSession();
        queryRepository = new JpaRepositoryFactory(session).getRepository(ExpiredEntityRepository.class);
        repository = new JpaExpiredEntityRepository(queryRepository);
    }

    @AfterEach
    void tearDown() {
        if (session != null) session.close();
        if (sessionFactory != null) sessionFactory.close();
    }


    @Test
    void deveManterProdutosQuandoMarcaOuDistribuidorNaoExistem() {
        UUID brandId = UUID.randomUUID();
        UUID distributorId = UUID.randomUUID();
        persist(
                new BrandEntity(brandId, distributorId, "Marca existente", "Racao"),
                new DistributorEntity(distributorId, "Distribuidor existente", "Centro",
                        List.of(), "Atacado", List.of(), "123"),
                product("Sem marca", UUID.randomUUID(), distributorId, true),
                product("Sem distribuidor", brandId, UUID.randomUUID(), true),
                product("Sem ambos", UUID.randomUUID(), UUID.randomUUID(), true));

        var result = repository.findAllExpiredProduct(PageRequest.of(0, 6));
        assertThat(result).hasSize(3);
        assertThat(result).filteredOn(p -> p.getNameProduct().equals("Sem marca"))
                .singleElement().satisfies(p -> {
                    assertThat(p.getNameBrand()).isNull();
                    assertThat(p.getNameDistributor()).isEqualTo("Distribuidor existente");
                });
        assertThat(result).filteredOn(p -> p.getNameProduct().equals("Sem distribuidor"))
                .singleElement().satisfies(p -> {
                    assertThat(p.getNameBrand()).isEqualTo("Marca existente");
                    assertThat(p.getNameDistributor()).isNull();
                });
        assertThat(result).filteredOn(p -> p.getNameProduct().equals("Sem ambos"))
                .singleElement().satisfies(p -> {
                    assertThat(p.getNameBrand()).isNull();
                    assertThat(p.getNameDistributor()).isNull();
                });
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaProdutosHabilitados() {
        persist(product("Produto ativo", UUID.randomUUID(), UUID.randomUUID(), false));
        assertThat(repository.findAllExpiredProduct(PageRequest.of(0, 6))).isEmpty();
    }

    @Test
    void deveBuscarDominioPeloIdProductComMarcaEDistribuidor() {
        UUID brandId = UUID.randomUUID();
        UUID distributorId = UUID.randomUUID();
        var product = product("Produto buscado", brandId, distributorId, false);
        persist(
                new BrandEntity(brandId, UUID.randomUUID(), "Marca correta", "Racao"),
                new DistributorEntity(distributorId, "Distribuidor correto", "Centro",
                        List.of(), "Atacado", List.of(), "123"),
                product,
                product("Outro produto", brandId, distributorId, true));

        var result = repository.findById(product.getIdProduct());

        assertThat(result.getNameBrand()).isEqualTo("Marca correta");
        assertThat(result.getNameDistributor()).isEqualTo("Distribuidor correto");
        assertThat(result).usingRecursiveComparison()
                .comparingOnlyFields("idProduct", "idBrand", "idDistributor", "nameProduct",
                        "dataCreated", "dataCreatedProduct", "dataDueProduct",
                        "lotProduct", "stockProduct", "dataDueProductVerify", "productEnableDue")
                .isEqualTo(product);
    }

    @Test
    void deveBuscarPeloIdMesmoSemMarcaEDistribuidor() {
        var product = product("Sem relacionamentos", UUID.randomUUID(), UUID.randomUUID(), true);
        persist(product);

        var result = repository.findById(product.getIdProduct());

        assertThat(result.getIdProduct()).isEqualTo(product.getIdProduct());
        assertThat(result.getNameBrand()).isNull();
        assertThat(result.getNameDistributor()).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoIdProductNaoExiste() {
        assertThatThrownBy(() -> repository.findById(UUID.randomUUID()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("product not found");
    }

    @Test
    void devePaginarProdutosSemRepetirRegistrosEntrePaginas() {
        UUID brandId = UUID.randomUUID();
        UUID distributorId = UUID.randomUUID();
        persist(
                product("Produto 1", brandId, distributorId, true),
                product("Produto 2", brandId, distributorId, true),
                product("Produto 3", brandId, distributorId, true),
                product("Produto 4", brandId, distributorId, true),
                product("Produto 5", brandId, distributorId, true),
                product("Produto 6", brandId, distributorId, true),
                product("Produto 7", brandId, distributorId, true));

        var useCase = new ExpiredFindAllUseCase(repository);
        var primeiraPagina = useCase.execute(0);
        var segundaPagina = useCase.execute(1);

        assertThat(primeiraPagina).hasSize(6);
        assertThat(segundaPagina).hasSize(1);
        assertThat(primeiraPagina.stream().map(p -> p.idProduct()).toList())
                .doesNotContain(segundaPagina.getFirst().idProduct());
        assertThat(useCase.execute(0)).isEqualTo(primeiraPagina);
        assertThat(useCase.execute(2)).isEmpty();
    }

    private void persist(Object... entities) {
        var transaction = session.beginTransaction();
        for (Object entity : entities) session.persist(entity);
        transaction.commit();
        session.clear();
    }

    private ProductEntity product(String name, UUID brandId, UUID distributorId, boolean enabled) {
        return new ProductEntity(UUID.randomUUID(), brandId, distributorId, name, "Racao", "Descricao",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 30),
                "0017-A", 10.0, 15.0, 9L, true, 2L, enabled);
    }
}
