package com.SystemPetShop.shared.infrastructure.persistence.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.SystemPetShop.metrics.infrastructure.repository",
                "com.SystemPetShop.brand.infrastructure.metrics",
                "com.SystemPetShop.distributor.infrastructure.metrics"
        },
        entityManagerFactoryRef = "metricsEntityManagerFactory",
        transactionManagerRef = "metricsTransactionManager"
)
public class MetricsDbConfig {

    @Bean("metricsDataSource")
    @ConfigurationProperties("app.datasource.productmetrics")
    public HikariDataSource metricsDataSource(){

        return new HikariDataSource();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean metricsEntityManagerFactory(
            @Qualifier("metricsDataSource") DataSource dataSource
    ){
        var factory = new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("metrics");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        factory.setPackagesToScan(
                "com.SystemPetShop.product.infrastructure.entity",
                "com.SystemPetShop.brand.infrastructure.entity",
                "com.SystemPetShop.distributor.infrastructure.entity"
        );

        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "validate",
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl"
        ));

        return factory;
    }

    @Bean
    public PlatformTransactionManager metricsTransactionManager(
            @Qualifier("metricsEntityManagerFactory")
            EntityManagerFactory entityManagerFactory
    ){
        return new JpaTransactionManager(entityManagerFactory);
    }
}
