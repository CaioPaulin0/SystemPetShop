package com.SystemPetShop.shared.infrastructure.persistence.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
                "com.SystemPetShop.product.infrastructure.repository",
                "com.SystemPetShop.brand.infrastructure.repository",
                "com.SystemPetShop.distributor.infrastructure.repository",
        },
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
public class MainDbConfig {

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.main")
    public HikariDataSource mainDataSource(){
        return new HikariDataSource();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            @Qualifier("mainDataSource") DataSource dataSource
    ){
        var factory = new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("main");
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
    @Primary
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory")
            EntityManagerFactory entityManagerFactory
    ){
        return new JpaTransactionManager(entityManagerFactory);
    }
}
