package com.SystemPetShop;

import com.SystemPetShop.product.application.ProductMetricsSearchUseCase;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import com.SystemPetShop.product.infrastructure.http.ProductController;
import com.SystemPetShop.product.infrastructure.repository.JpaProductEntityRepository;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import com.SystemPetShop.product.infrastructure.repository.ProductEntityRepository;
import com.SystemPetShop.shared.infrastructure.persistence.config.MainDbConfig;
import com.SystemPetShop.shared.infrastructure.persistence.config.MetricsDbConfig;
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
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductMetricsSearchTest {
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
                    dataSource.setJdbcUrl("jdbc:h2:mem:product_metrics_search_" + beanName + UUID.randomUUID());
                    dataSource.setUsername("sa");
                    dataSource.setPassword("");
                }
                if (bean instanceof LocalContainerEntityManagerFactoryBean factory) {
                    factory.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "create-drop");
                }
                return bean;
            }
        });
        context.register(MainDbConfig.class, MetricsDbConfig.class,
                JpaProductEntityRepository.class, ProductMetricsSearchUseCase.class);
        context.refresh();
    }

    @BeforeEach
    void setUp() {
        context.getBean(ProductEntityRepository.class).deleteAll();
        context.getBean(ProductMetricsRepository.class).deleteAll();
        var controller = new ProductController(null, null, null, null, null, null, null,
                context.getBean(ProductMetricsSearchUseCase.class), null);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @AfterAll
    static void stopDatabases() {
        if (context != null) context.close();
    }

    @Test
    void deveBuscarSomenteNoBancoMetrics() throws Exception {
        context.getBean(ProductEntityRepository.class).save(product("Racao exclusiva main"));
        persist("Racao exclusiva metrics");

        mvc.perform(get("/product/metrics/search").param("name", "Racao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Racao exclusiva metrics"));
    }

    @Test
    void deveBuscarTrechoNoMeioDoNomeIgnorandoCaixaEEspacosExternos() throws Exception {
        persist("Super Racao Premium", "Super Racao Filhotes", "Brinquedo");

        mvc.perform(get("/product/metrics/search").param("name", "  rAcAo  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nameProduct").value("Super Racao Filhotes"))
                .andExpect(jsonPath("$[1].nameProduct").value("Super Racao Premium"))
                .andExpect(jsonPath("$[0].idProduct").isNotEmpty())
                .andExpect(jsonPath("$[0].typeProduct").value("Alimento"))
                .andExpect(jsonPath("$[0].descriptionProduct").value("Descricao do produto"))
                .andExpect(jsonPath("$[0].lotProduct").value("001-A"));
    }

    @Test
    void deveBuscarNomeCompleto() throws Exception {
        persist("Racao Premium", "Racao Filhotes");

        mvc.perform(get("/product/metrics/search").param("name", "Racao Premium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Racao Premium"));
    }

    @Test
    void devePaginarSeisItensComOrdemEstavelSemRepetirProdutos() throws Exception {
        persist("Racao 7", "Racao 3", "Racao 1", "Racao 6", "Racao 4", "Racao 2", "Racao 5", "Brinquedo");

        for (int attempt = 0; attempt < 2; attempt++) {
            var result = mvc.perform(get("/product/metrics/search").param("name", "Racao").param("page", "0").param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(6)));
            for (int index = 0; index < 6; index++) {
                result.andExpect(jsonPath("$[" + index + "].nameProduct").value("Racao " + (index + 1)));
            }
        }
        mvc.perform(get("/product/metrics/search").param("name", "Racao").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Racao 7"));
        mvc.perform(get("/product/metrics/search").param("name", "Racao").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrar() throws Exception {
        persist("Racao Premium");
        mvc.perform(get("/product/metrics/search").param("name", "Coleira"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%", "_"})
    void deveTratarCuringasSqlComoTextoLiteral(String name) throws Exception {
        persist("Racao Premium", "Produto " + name);
        mvc.perform(get("/product/metrics/search").param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nameProduct").value("Produto " + name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void deveRejeitarNomeVazio(String name) throws Exception {
        mvc.perform(get("/product/metrics/search").param("name", name))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveExigirNome() throws Exception {
        mvc.perform(get("/product/metrics/search"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "abc", "2147483648"})
    void deveRejeitarPaginaInvalida(String page) throws Exception {
        mvc.perform(get("/product/metrics/search").param("name", "Racao").param("page", page))
                .andExpect(status().isBadRequest());
    }

    private void persist(String... names) {
        for (String name : names) {
            context.getBean(ProductMetricsRepository.class).save(product(name));
        }
    }

    private ProductEntity product(String name) {
        return new ProductEntity(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                name, "Alimento", "Descricao do produto", LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), "001-A",
                10.0, 15.0, 9L, true, 1L, true);
    }
}
