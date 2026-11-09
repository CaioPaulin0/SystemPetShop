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
                "com.SystemPetShop.product.infrastructure.messaging.kafka.repository",
                "com.SystemPetShop.expired.infrastructure.repository",
                "com.SystemPetShop.distributor.infrastructure.messaging.kafka.repository",
                "com.SystemPetShop.brand.infrastructure.messaging.kafka.repository"
        },
        entityManagerFactoryRef = "expiredEntityManagerFactory",
        transactionManagerRef = "expiredTransactionManager"
)
public class ExpiredDbConfig {

    @Bean
    @ConfigurationProperties("app.datasource.productexpired")
    public HikariDataSource expiredDataSource(){
        return new HikariDataSource();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean expiredEntityManagerFactory(
            @Qualifier("expiredDataSource") DataSource dataSource
    ){
        var factory = new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("expired");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        factory.setPackagesToScan(
                "com.SystemPetShop.product.infrastructure.entity",
                "com.SystemPetShop.brand.infrastructure.entity",
                "com.SystemPetShop.distributor.infrastructure.entity",
                "com.SystemPetShop.expired.infrastructure.entity"
        );


        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "validate",
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl"
        ));

        return factory;
    }

    @Bean
    public PlatformTransactionManager expiredTransactionManager(
            @Qualifier("expiredEntityManagerFactory")
            EntityManagerFactory entityManagerFactory
    ){
        return new JpaTransactionManager(entityManagerFactory);
    }
}
