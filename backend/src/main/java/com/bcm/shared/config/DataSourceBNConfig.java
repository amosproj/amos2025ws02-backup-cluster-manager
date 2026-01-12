package com.bcm.shared.config;

import com.bcm.shared.repository.*;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
@EnableR2dbcRepositories(
        basePackages = "com.bcm.shared.repository",
        entityOperationsRef = "bnTemplate"
)
public class DataSourceBNConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc.bn")
    public R2dbcProperties bnR2dbcProps() {
        return new R2dbcProperties();
    }

    @Bean("bnConnectionFactory")
    public ConnectionFactory bnConnectionFactory(@Qualifier("bnR2dbcProps") R2dbcProperties props) {
        ConnectionFactoryOptions base = ConnectionFactoryOptions.parse(props.getUrl());
        ConnectionFactoryOptions.Builder b = ConnectionFactoryOptions.builder().from(base);

        if (props.getUsername() != null) b.option(ConnectionFactoryOptions.USER, props.getUsername());
        if (props.getPassword() != null) b.option(ConnectionFactoryOptions.PASSWORD, props.getPassword());

        return ConnectionFactories.get(b.build());
    }

    @Bean("bnTemplate")
    public R2dbcEntityTemplate bnTemplate(@Qualifier("bnConnectionFactory") ConnectionFactory cf) {
        return new R2dbcEntityTemplate(cf);
    }

    @Bean("bnTxManager")
    public ReactiveTransactionManager bnTxManager(@Qualifier("bnConnectionFactory") ConnectionFactory cf) {
        return new R2dbcTransactionManager(cf);
    }

}
