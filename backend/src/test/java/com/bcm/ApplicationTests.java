package com.bcm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class ApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("postgres")
			.withUsername("appuser")
			.withPassword("apppassword");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		try (Connection conn = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
			 Statement stmt = conn.createStatement()) {
			for (String db : new String[]{"bcm_node0", "bcm"}) {
				try {
					stmt.executeUpdate("CREATE DATABASE " + db + " OWNER " + postgres.getUsername());
				} catch (Exception e) {
					if (!e.getMessage().contains("already exists")) throw new RuntimeException("Failed to create " + db, e);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Database creation failed", e);
		}

		String h = postgres.getHost();
		int p = postgres.getFirstMappedPort();
		String u = postgres.getUsername();
		String pw = postgres.getPassword();

		registry.add("spring.datasource.hikari.jdbc-url", () -> String.format("jdbc:postgresql://%s:%d/bcm_node0", h, p));
		registry.add("spring.datasource.hikari.username", () -> u);
		registry.add("spring.datasource.hikari.password", () -> pw);
		registry.add("spring.cm-datasource.hikari.jdbc-url", () -> String.format("jdbc:postgresql://%s:%d/bcm", h, p));
		registry.add("spring.cm-datasource.hikari.username", () -> u);
		registry.add("spring.cm-datasource.hikari.password", () -> pw);
		registry.add("spring.r2dbc.bn.url", () -> String.format("r2dbc:postgresql://%s:%d/bcm_node0", h, p));
		registry.add("spring.r2dbc.bn.username", () -> u);
		registry.add("spring.r2dbc.bn.password", () -> pw);
		registry.add("spring.r2dbc.cm.url", () -> String.format("r2dbc:postgresql://%s:%d/bcm", h, p));
		registry.add("spring.r2dbc.cm.username", () -> u);
		registry.add("spring.r2dbc.cm.password", () -> pw);
	}

	@Test
	void contextLoads() {
	}

}
