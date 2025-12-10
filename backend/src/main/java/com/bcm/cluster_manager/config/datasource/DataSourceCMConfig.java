package com.bcm.cluster_manager.config.datasource;

import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
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
public class DataSourceCMConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.cm")
    public DataSourceProperties cmDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSourceCM() {
        return cmDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryCM(
            @Qualifier("dataSourceCM") DataSource ds) throws Exception {

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        return factory.getObject();
    }

    @Bean(name = "userMapperCM")
    public MapperFactoryBean<UserMapper> userMapperCM(
            @Qualifier("sqlSessionFactoryCM") SqlSessionFactory factory) {

        MapperFactoryBean<UserMapper> bean = new MapperFactoryBean<>(UserMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean(name = "groupMapperCM")
    public MapperFactoryBean<GroupMapper> groupMapperCM(
            @Qualifier("sqlSessionFactoryCM") SqlSessionFactory factory) {
        MapperFactoryBean<GroupMapper> bean = new MapperFactoryBean<>(GroupMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean(name = "userGroupRelationMapperCM")
    public MapperFactoryBean<UserGroupRelationMapper> userGroupRelationMapperCM(
            @Qualifier("sqlSessionFactoryCM") SqlSessionFactory factory) {
        MapperFactoryBean<UserGroupRelationMapper> bean = new MapperFactoryBean<>(UserGroupRelationMapper.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }
}
