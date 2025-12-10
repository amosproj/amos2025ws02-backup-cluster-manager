package com.bcm.shared.config;

import com.bcm.shared.repository.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceBNConfig {


    @Bean
    @ConfigurationProperties("spring.datasource.bn")
    public DataSourceProperties bnDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSourceBN() {
        return bnDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryBN(
            @Qualifier("dataSourceBN") DataSource ds) throws Exception {

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        return factory.getObject();
    }

    @Bean(name = "backupMapperBN")
    public MapperFactoryBean<BackupMapper> backupMapperBN(
            @Qualifier("sqlSessionFactoryBN") SqlSessionFactory factory) {
        MapperFactoryBean<BackupMapper> bean = new MapperFactoryBean<>(BackupMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean(name = "clientMapperBN")
    public MapperFactoryBean<ClientMapper> clientMapperBN(
            @Qualifier("sqlSessionFactoryBN") SqlSessionFactory factory) {
        MapperFactoryBean<ClientMapper> bean = new MapperFactoryBean<>(ClientMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean(name = "groupMapperBN")
    public MapperFactoryBean<GroupMapper> groupMapperBN(
            @Qualifier("sqlSessionFactoryBN") SqlSessionFactory factory) {
        MapperFactoryBean<GroupMapper> bean = new MapperFactoryBean<>(GroupMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean(name = "taskMapperBN")
    public MapperFactoryBean<TaskMapper> taskMapperBN(
            @Qualifier("sqlSessionFactoryBN") SqlSessionFactory factory) {
        MapperFactoryBean<TaskMapper> bean = new MapperFactoryBean<>(TaskMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean(name = "userGroupRelationMapperBN")
    public MapperFactoryBean<UserGroupRelationMapper> userGroupRelationMapperBN(
            @Qualifier("sqlSessionFactoryBN") SqlSessionFactory factory) {
        MapperFactoryBean<UserGroupRelationMapper> bean = new MapperFactoryBean<>(UserGroupRelationMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean(name = "userMapperBN")
    public MapperFactoryBean<UserMapper> userMapperBN(
            @Qualifier("sqlSessionFactoryBN") SqlSessionFactory factory) {
        MapperFactoryBean<UserMapper> bean = new MapperFactoryBean<>(UserMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }
}
