package com.bcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot application entry point for the Backup Cluster Manager.
 * Enables scheduling, caching, and component scanning for the shared package.
 */
@EnableScheduling
@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = {
		"com.bcm.shared",
})
public class Application {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
