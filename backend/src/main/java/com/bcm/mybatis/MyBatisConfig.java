package com.bcm.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.bcm.mybatis.mappers")
public class MyBatisConfig {}
