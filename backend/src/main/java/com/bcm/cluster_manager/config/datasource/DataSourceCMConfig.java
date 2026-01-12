package com.bcm.cluster_manager.config.datasource;

import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import javax.sql.DataSource;

@Configuration
@Profile("cluster_manager")
@EnableR2dbcRepositories(
        basePackages = "com.bcm.cluster_manager.repository",
        entityOperationsRef = "cmTemplate"
)
public class DataSourceCMConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc.cm")
    public R2dbcProperties cmR2dbcProps() {
        return new R2dbcProperties();
    }

    @Bean("cmConnectionFactory")
    public ConnectionFactory cmConnectionFactory(@Qualifier("cmR2dbcProps") R2dbcProperties props) {
        ConnectionFactoryOptions base = ConnectionFactoryOptions.parse(props.getUrl());
        ConnectionFactoryOptions.Builder b = ConnectionFactoryOptions.builder().from(base);

        if (props.getUsername() != null) b.option(ConnectionFactoryOptions.USER, props.getUsername());
        if (props.getPassword() != null) b.option(ConnectionFactoryOptions.PASSWORD, props.getPassword());

        return ConnectionFactories.get(b.build());
    }


    @Bean("cmTemplate")
    public R2dbcEntityTemplate cmTemplate(@Qualifier("cmConnectionFactory") ConnectionFactory cf) {
        return new R2dbcEntityTemplate(cf);
    }

    @Bean("cmTxManager")
    public ReactiveTransactionManager cmTxManager(@Qualifier("cmConnectionFactory") ConnectionFactory cf) {
        return new R2dbcTransactionManager(cf);
    }
}
