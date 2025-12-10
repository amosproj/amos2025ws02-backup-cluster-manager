package com.bcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {
		"com.bcm.shared",
})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
