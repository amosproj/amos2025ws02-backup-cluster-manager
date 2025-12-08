package com.bcm.cluster_manager.config.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        basePackages = "com.bcm.cluster_manager",
        sqlSessionFactoryRef = "sqlSessionFactoryCM"
)
public class DataSourceCMConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.cm")
    public DataSource dataSourceCM() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryCM(
            @Qualifier("dataSourceCM") DataSource dsCM) throws Exception {

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dsCM);
        return factory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplateCM(
            @Qualifier("sqlSessionFactoryCM") SqlSessionFactory sfCM) {

        return new SqlSessionTemplate(sfCM);
    }
}