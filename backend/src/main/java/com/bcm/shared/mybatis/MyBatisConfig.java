package com.bcm.shared.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.bcm.shared.repository")
public class MyBatisConfig {}
