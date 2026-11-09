package com.SystemPetShop;

import com.SystemPetShop.brand.application.BrandSearchUseCase;
import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;
import com.SystemPetShop.brand.infrastructure.http.BrandController;
import com.SystemPetShop.brand.infrastructure.metrics.BrandMetricsRepository;
import com.SystemPetShop.brand.infrastructure.repository.BrandEntityRepository;
import com.SystemPetShop.brand.infrastructure.repository.JpaBrandRepository;
import com.SystemPetShop.distributor.application.DistributorSearchUseCase;
import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.distributor.infrastructure.http.DistributorController;
import com.SystemPetShop.distributor.infrastructure.metrics.DistributorMetricsRepository;
import com.SystemPetShop.distributor.infrastructure.repository.DistributorRepositoryEntity;
import com.SystemPetShop.distributor.infrastructure.repository.JpaDistributorRepository;
import com.SystemPetShop.shared.infrastructure.persistence.config.MainDbConfig;
import com.SystemPetShop.product.application.ProductSearchUseCase;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import com.SystemPetShop.product.infrastructure.http.ProductController;
import com.SystemPetShop.product.infrastructure.repository.JpaProductEntityRepository;
import com.SystemPetShop.product.infrastructure.repository.ProductEntityRepository;
import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import com.SystemPetShop.shared.infrastructure.persistence.config.MetricsDbConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MainSearchTest {
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
                    dataSource.setJdbcUrl("jdbc:h2:mem:main_search_" + beanName + UUID.randomUUID());
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
                JpaBrandRepository.class, BrandSearchUseCase.class,
                JpaDistributorRepository.class, DistributorSearchUseCase.class,
                JpaProductEntityRepository.class, ProductSearchUseCase.class);
        context.refresh();
    }

    @BeforeEach
    void setUp() {
        context.getBean(ProductEntityRepository.class).deleteAll();
        context.getBean(ProductMetricsRepository.class).deleteAll();
        context.getBean(BrandEntityRepository.class).deleteAll();
        context.getBean(BrandMetricsRepository.class).deleteAll();
        context.getBean(DistributorRepositoryEntity.class).deleteAll();
        context.getBean(DistributorMetricsRepository.class).deleteAll();
        mvc = MockMvcBuilders.standaloneSetup(
                new BrandController(null, null, null, null, null,
                        null, context.getBean(BrandSearchUseCase.class)),
                new DistributorController(null, null, null, null, null,
                        null, context.getBean(DistributorSearchUseCase.class)),
                new ProductController(null, null, null, null, null, null, null, null,
                        context.getBean(ProductSearchUseCase.class))).build();
    }

    @AfterAll
    static void stopDatabases() {
        if (context != null) context.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"brand", "distributor", "product"})
    void deveBuscarSomenteNoBancoMain(String resource) throws Exception {
        if (resource.equals("brand")) {
            context.getBean(BrandMetricsRepository.class).save(brand("Pet exclusivo metrics"));
        } else if (resource.equals("product")) {
            context.getBean(ProductMetricsRepository.class).save(product("Pet exclusivo metrics"));
        } else {
            context.getBean(DistributorMetricsRepository.class).save(distributor("Pet exclusivo metrics"));
        }
        persist(resource, "Pet exclusivo main");

        mvc.perform(get(route(resource)).param("name", "Pet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]." + nameField(resource)).value("Pet exclusivo main"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"brand", "distributor", "product"})
    void deveBuscarTrechoIgnorandoCaixaEEspacosEPreservarDados(String resource) throws Exception {
        persist(resource, "Super Pet Premium", "Super Pet Filhotes", "Outro");
        var result = mvc.perform(get(route(resource)).param("name", "  pEt  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]." + nameField(resource)).value("Super Pet Filhotes"))
                .andExpect(jsonPath("$[1]." + nameField(resource)).value("Super Pet Premium"));
        if (resource.equals("brand")) {
            result.andExpect(jsonPath("$[0].idBrand").isNotEmpty())
                    .andExpect(jsonPath("$[0].idDistributor").isNotEmpty())
                    .andExpect(jsonPath("$[0].typeBrand").value("Alimento"));
        } else if (resource.equals("product")) {
            result.andExpect(jsonPath("$[0].idProduct").isNotEmpty())
                    .andExpect(jsonPath("$[0].typeProduct").value("Alimento"))
                    .andExpect(jsonPath("$[0].descriptionProduct").value("Descricao do produto"))
                    .andExpect(jsonPath("$[0].lotProduct").value("001-A"));
        } else {
            result.andExpect(jsonPath("$[0].id").isNotEmpty())
                    .andExpect(jsonPath("$[0].typeDistributor").value("Atacado"))
                    .andExpect(jsonPath("$[0].localizationDistributor").value("Centro"))
                    .andExpect(jsonPath("$[0].salespeopleDistributor[0]").value("Ana"))
                    .andExpect(jsonPath("$[0].telephones[0]").value("11999999999"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"brand", "distributor", "product"})
    void deveBuscarNomeCompleto(String resource) throws Exception {
        persist(resource, "Pet Premium", "Pet Filhotes");
        mvc.perform(get(route(resource)).param("name", "Pet Premium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]." + nameField(resource)).value("Pet Premium"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"brand", "distributor", "product"})
    void devePaginarSeisItensSemRepetirRegistros(String resource) throws Exception {
        persist(resource, "Pet 7", "Pet 3", "Pet 1", "Pet 6", "Pet 4", "Pet 2", "Pet 5", "Outro");
        for (int attempt = 0; attempt < 2; attempt++) {
            var result = mvc.perform(get(route(resource)).param("name", "Pet").param("page", "0").param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(6)));
            for (int index = 0; index < 6; index++) {
                result.andExpect(jsonPath("$[" + index + "]." + nameField(resource)).value("Pet " + (index + 1)));
            }
        }
        mvc.perform(get(route(resource)).param("name", "Pet").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]." + nameField(resource)).value("Pet 7"));
        mvc.perform(get(route(resource)).param("name", "Pet").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"brand", "distributor", "product"})
    void deveRetornarListaVaziaSemCorrespondencia(String resource) throws Exception {
        persist(resource, "Pet Premium");
        mvc.perform(get(route(resource)).param("name", "Ausente"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"brand", "distributor", "product"})
    void deveTratarCuringasSqlComoTextoLiteral(String resource) throws Exception {
        persist(resource, "Pet Premium", "Pet %", "Pet _");
        for (String name : List.of("%", "_")) {
            mvc.perform(get(route(resource)).param("name", name))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0]." + nameField(resource)).value("Pet " + name));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"brand", "distributor", "product"})
    void deveRejeitarParametrosInvalidos(String resource) throws Exception {
        mvc.perform(get(route(resource))).andExpect(status().isBadRequest());
        for (String name : List.of("", "   ")) {
            mvc.perform(get(route(resource)).param("name", name)).andExpect(status().isBadRequest());
        }
        for (String page : List.of("-1", "abc", "2147483648")) {
            mvc.perform(get(route(resource)).param("name", "Pet").param("page", page))
                    .andExpect(status().isBadRequest());
        }
    }

    private String route(String resource) {
        return "/" + resource + "/search";
    }

    private String nameField(String resource) {
        return switch (resource) {
            case "brand" -> "nameBrand";
            case "product" -> "nameProduct";
            default -> "nameDistributor";
        };
    }

    private void persist(String resource, String... names) {
        for (String name : names) {
            if (resource.equals("brand")) {
                context.getBean(BrandEntityRepository.class).save(brand(name));
            } else if (resource.equals("product")) {
                context.getBean(ProductEntityRepository.class).save(product(name));
            } else {
                context.getBean(DistributorRepositoryEntity.class).save(distributor(name));
            }
        }
    }

    private BrandEntity brand(String name) {
        return new BrandEntity(UUID.randomUUID(), UUID.randomUUID(), name, "Alimento");
    }

    private ProductEntity product(String name) {
        return new ProductEntity(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                name, "Alimento", "Descricao do produto", LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), "001-A",
                10.0, 15.0, 9L, true, 1L, true);
    }

    private DistributorEntity distributor(String name) {
        return new DistributorEntity(UUID.randomUUID(), name, "Centro", List.of("Ana"), "Atacado",
                List.of("11999999999"), "12345678000190");
    }
}
