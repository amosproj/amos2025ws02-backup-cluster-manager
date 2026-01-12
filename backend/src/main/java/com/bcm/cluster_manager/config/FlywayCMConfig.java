package com.bcm.cluster_manager.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(prefix="app.flyway.base", name="enabled", havingValue="true")
public class FlywayCMConfig {

    // Cluster manager Flyway (runs only if profile active)
    @Bean(initMethod = "migrate")
    @Profile("cluster_manager")
    public Flyway cmFlyway( DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/cluster_manager")
                .baselineOnMigrate(true)
                .load();
    }

}