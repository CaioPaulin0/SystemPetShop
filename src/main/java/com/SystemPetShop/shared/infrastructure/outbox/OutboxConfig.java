package com.SystemPetShop.shared.infrastructure.outbox;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.Clock;

@Configuration
public class OutboxConfig {
    @Bean("mainOutbox")
    public OutboxStore mainOutbox(@Qualifier("mainDataSource") DataSource dataSource,
                                 @Qualifier("mainTransactionManager") PlatformTransactionManager manager,
                                 @Value("${app.outbox.retry-delay-ms:1000}") long retryDelay,
                                 @Value("${app.outbox.max-retry-delay-ms:60000}") long maxRetryDelay) {
        return new OutboxStore(dataSource, manager, Clock.systemUTC(), retryDelay, maxRetryDelay);
    }

    @Bean("expiredOutbox")
    public OutboxStore expiredOutbox(@Qualifier("expiredDataSource") DataSource dataSource,
                                    @Qualifier("expiredTransactionManager") PlatformTransactionManager manager,
                                    @Value("${app.outbox.retry-delay-ms:1000}") long retryDelay,
                                    @Value("${app.outbox.max-retry-delay-ms:60000}") long maxRetryDelay) {
        return new OutboxStore(dataSource, manager, Clock.systemUTC(), retryDelay, maxRetryDelay);
    }
}
