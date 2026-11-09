package com.SystemPetShop;

import com.SystemPetShop.expired.application.ExpiredSearchUseCase;
import com.SystemPetShop.expired.application.ExpiredFilterUseCase;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import com.SystemPetShop.expired.infrastructure.http.ExpiredController;
import com.SystemPetShop.expired.infrastructure.repository.JpaExpiredEntityRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.repository.ProductExpiredRepositoryEntity;
import com.SystemPetShop.product.infrastructure.repository.ProductEntityRepository;
import com.SystemPetShop.shared.infrastructure.persistence.config.MainDbConfig;
import com.SystemPetShop.shared.infrastructure.persistence.config.MetricsDbConfig;
import com.SystemPetShop.shared.infrastructure.persistence.config.ExpiredDbConfig;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.repository.BrandExpiredRepositoryEntity;
import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.repository.DistributorExpiredRepositoryEntity;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExpiredSearchTest {
    private static AnnotationConfigApplicationContext context;
    private MockMvc mvc;

    @BeforeAll
    static void startDatabases() {
        context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof HikariDataSource dataSource) {
                    dataSource.setDriverClassName("org.h2.Driver");
                    dataSource.setJdbcUrl("jdbc:h2:mem:expired_search_" + beanName + UUID.randomUUID());
                    dataSource.setUsername("sa");
                    dataSource.setPassword("");
                }
                if (bean instanceof LocalContainerEntityManagerFactoryBean factory) {
                    factory.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
                }
                return bean;
            }
        });
        context.register(MainDbConfig.class, MetricsDbConfig.class, ExpiredDbConfig.class,
                JpaExpiredEntityRepository.class, ExpiredSearchUseCase.class,
                ExpiredFilterUseCase.class);
        context.refresh();
    }

    @BeforeEach
    void setUp() {
        context.getBean(ProductEntityRepository.class).deleteAll();
        context.getBean(ProductMetricsRepository.class).deleteAll();
        context.getBean(ProductExpiredRepositoryEntity.class).deleteAll();
        context.getBean(BrandExpiredRepositoryEntity.class).deleteAll();
        context.getBean(DistributorExpiredRepositoryEntity.class).deleteAll();
        var controller = new ExpiredController(null, null,
                context.getBean(ExpiredSearchUseCase.class),
                context.getBean(ExpiredFilterUseCase.class));
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterAll
    static void stopDatabases() {
        if (context != null) context.close();
    }

    @Test
    void deveBuscarSomenteNoBancoExpired() throws Exception {
        context.getBean(ProductEntityRepository.class).save(product("Racao exclusiva main"));
        context.getBean(ProductMetricsRepository.class).save(product("Racao exclusiva metrics"));
        persist("Racao exclusiva expired");

        mvc.perform(get("/expired/search").param("name", "Racao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Racao exclusiva expired"));
    }

    @Test
    void deveBuscarTrechoNoMeioDoNomeIgnorandoCaixaEEspacosExternos() throws Exception {
        persist("Super Racao Premium", "Super Racao Filhotes", "Brinquedo");

        mvc.perform(get("/expired/search").param("name", "  rAcAo  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nameProduct").value("Super Racao Filhotes"))
                .andExpect(jsonPath("$[1].nameProduct").value("Super Racao Premium"))
                .andExpect(jsonPath("$[0].idProduct").isNotEmpty())
                .andExpect(jsonPath("$[0].stockProduct").value(9))
                .andExpect(jsonPath("$[0].dataDueProduct").value("2027-01-01"))
                .andExpect(jsonPath("$[0].lotProduct").value("001-A"));
    }

    @Test
    void deveBuscarNomeCompleto() throws Exception {
        persist("Racao Premium", "Racao Filhotes");

        mvc.perform(get("/expired/search").param("name", "Racao Premium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Racao Premium"));
    }

    @Test
    void devePaginarSeisItensComOrdemEstavelSemRepetirProdutos() throws Exception {
        persist("Racao 7", "Racao 3", "Racao 1", "Racao 6", "Racao 4", "Racao 2", "Racao 5", "Brinquedo");

        for (int attempt = 0; attempt < 2; attempt++) {
            var result = mvc.perform(get("/expired/search").param("name", "Racao").param("page", "0").param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(6)));
            for (int index = 0; index < 6; index++) {
                result.andExpect(jsonPath("$[" + index + "].nameProduct").value("Racao " + (index + 1)));
            }
        }
        mvc.perform(get("/expired/search").param("name", "Racao").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Racao 7"));
        mvc.perform(get("/expired/search").param("name", "Racao").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrar() throws Exception {
        persist("Racao Premium");
        mvc.perform(get("/expired/search").param("name", "Coleira"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_", "!", "!%"})
    void deveTratarCuringasSqlComoTextoLiteral(String name) throws Exception {
        persist("Racao Premium", "Produto " + name);
        mvc.perform(get("/expired/search").param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Produto " + name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void deveRejeitarNomeVazio(String name) throws Exception {
        mvc.perform(get("/expired/search").param("name", name))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExigirNome() throws Exception {
        mvc.perform(get("/expired/search"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "2147483648"})
    void deveRejeitarPaginaInvalida(String page) throws Exception {
        mvc.perform(get("/expired/search").param("name", "Racao").param("page", page))
                .andExpect(status().isBadRequest());
    }

    private void persist(String... names) {
        for (String name : names) {
            context.getBean(ProductExpiredRepositoryEntity.class).save(product(name));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deveFiltrarPeloBooleanSomenteNoBancoExpired(boolean verified) throws Exception {
        context.getBean(ProductEntityRepository.class).save(product("Produto main"));
        context.getBean(ProductMetricsRepository.class).save(product("Produto metrics"));
        context.getBean(ProductExpiredRepositoryEntity.class).saveAll(List.of(
                product("Produto verificado", "A", 1L, UUID.randomUUID(), UUID.randomUUID(), true),
                product("Produto nao verificado", "B", 1L, UUID.randomUUID(), UUID.randomUUID(), false)));

        mvc.perform(get("/expired/filter").param("dataDueProductVerify", String.valueOf(verified)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value(
                        verified ? "Produto verificado" : "Produto nao verificado"))
                .andExpect(jsonPath("$[0].dataDueProductVerify").value(verified))
                .andExpect(jsonPath("$[0].dataDueProduct").value("2027-01-01"));
    }

    @Test
    void deveFiltrarSomenteDepoisDeSelecionarAVersaoMaisRecenteDoLote() throws Exception {
        context.getBean(ProductExpiredRepositoryEntity.class).saveAll(List.of(
                product("Produto antigo", "A", 1L, UUID.randomUUID(), UUID.randomUUID(), true),
                product("Produto atual", "A", 2L, UUID.randomUUID(), UUID.randomUUID(), false)));

        mvc.perform(get("/expired/filter").param("dataDueProductVerify", "true"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(get("/expired/filter").param("dataDueProductVerify", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Produto atual"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void devePaginarOitoResultadosFiltradosComOrdemEstavel(boolean verified) throws Exception {
        var repository = context.getBean(ProductExpiredRepositoryEntity.class);
        for (int index = 9; index >= 1; index--) {
            repository.save(product("Produto " + index, "Lote " + index, 1L,
                    UUID.randomUUID(), UUID.randomUUID(), verified));
            repository.save(product("Ignorado " + index, "Outro " + index, 1L,
                    UUID.randomUUID(), UUID.randomUUID(), !verified));
        }

        for (int attempt = 0; attempt < 2; attempt++) {
            var result = mvc.perform(get("/expired/filter")
                            .param("dataDueProductVerify", String.valueOf(verified)).param("page", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(8)));
            for (int index = 0; index < 8; index++) {
                result.andExpect(jsonPath("$[" + index + "].nameProduct").value("Produto " + (index + 1)))
                        .andExpect(jsonPath("$[" + index + "].dataDueProductVerify").value(verified));
            }
        }
        mvc.perform(get("/expired/filter")
                        .param("dataDueProductVerify", String.valueOf(verified)).param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Produto 9"));
        mvc.perform(get("/expired/filter")
                        .param("dataDueProductVerify", String.valueOf(verified)).param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deveExigirBooleanNoFiltro() throws Exception {
        mvc.perform(get("/expired/filter"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc"})
    void deveRejeitarBooleanInvalidoNoFiltro(String value) throws Exception {
        mvc.perform(get("/expired/filter").param("dataDueProductVerify", value))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "2147483648"})
    void deveRejeitarPaginaInvalidaNoFiltro(String page) throws Exception {
        mvc.perform(get("/expired/filter").param("dataDueProductVerify", "true").param("page", page))
                .andExpect(status().isBadRequest());
    }

    private ProductEntity product(String name) {
        return product(name, "001-A", 1L, UUID.randomUUID(), UUID.randomUUID());
    }

    private ProductEntity product(String name, String lot, long version, UUID brandId, UUID distributorId) {
        return product(name, lot, version, brandId, distributorId, true);
    }

    private ProductEntity product(String name, String lot, long version, UUID brandId, UUID distributorId,
                                  boolean verified) {
        return new ProductEntity(UUID.randomUUID(), brandId, distributorId,
                name, "Alimento", "Descricao do produto", LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), lot,
                10.0, 15.0, 9L, verified, version, true);
    }

    @Test
    void deveBuscarApenasAVersaoMaisRecenteDeCadaLote() throws Exception {
        var repository = context.getBean(ProductExpiredRepositoryEntity.class);
        UUID brandId = UUID.randomUUID();
        UUID distributorId = UUID.randomUUID();
        repository.saveAll(List.of(
                product("Racao antiga", "A", 1L, brandId, distributorId),
                product("Racao atual", "A", 2L, brandId, distributorId),
                product("Racao outro lote", "B", 1L, brandId, distributorId),
                product("Racao removida", "C", 1L, brandId, distributorId),
                product("Brinquedo atual", "C", 2L, brandId, distributorId)));

        mvc.perform(get("/expired/search").param("name", "Racao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nameProduct").value("Racao atual"))
                .andExpect(jsonPath("$[1].nameProduct").value("Racao outro lote"));
    }

    @Test
    void deveIncluirNomesDosRelacionamentosSemExcluirProdutosComRelacionamentosAusentes() throws Exception {
        UUID brandId = UUID.randomUUID();
        UUID distributorId = UUID.randomUUID();
        context.getBean(BrandExpiredRepositoryEntity.class)
                .save(new BrandEntity(brandId, distributorId, "Marca teste", "Alimento"));
        context.getBean(DistributorExpiredRepositoryEntity.class)
                .save(new DistributorEntity(distributorId, "Distribuidor teste", "Centro",
                        List.of(), "Atacado", List.of(), "123"));
        context.getBean(ProductExpiredRepositoryEntity.class).saveAll(List.of(
                product("Racao A completa", "A", 1L, brandId, distributorId),
                product("Racao B sem marca", "B", 1L, UUID.randomUUID(), distributorId),
                product("Racao C sem distribuidor", "C", 1L, brandId, UUID.randomUUID()),
                product("Racao D sem ambos", "D", 1L, UUID.randomUUID(), UUID.randomUUID())));

        mvc.perform(get("/expired/search").param("name", "Racao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].nameBrand").value("Marca teste"))
                .andExpect(jsonPath("$[0].nameDistributor").value("Distribuidor teste"))
                .andExpect(jsonPath("$[1].nameBrand").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$[1].nameDistributor").value("Distribuidor teste"))
                .andExpect(jsonPath("$[2].nameBrand").value("Marca teste"))
                .andExpect(jsonPath("$[2].nameDistributor").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$[3].nameBrand").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$[3].nameDistributor").value(org.hamcrest.Matchers.nullValue()));
    }
}
