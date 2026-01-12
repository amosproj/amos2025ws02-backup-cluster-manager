package com.bcm.shared.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true")
public class FlywayBaseConfig {

    // Base Flyway (always runs)
    @Bean(initMethod = "migrate")
    public Flyway baseFlyway( DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/base")
                .baselineOnMigrate(true)
                .load();
    }

}