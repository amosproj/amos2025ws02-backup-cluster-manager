package com.bcm.shared.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.Network;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class NodeRegistrationE2ETest {

    // Etablieren eines gemeinsamen Netzwerks für alle Container
    static Network network = Network.newNetwork();

    private static final String NETWORK_ALIAS_DB = "postgres-db"; // Fester Alias für DB
    private static final String NETWORK_ALIAS_CM = "cm-node";
    private static final String NETWORK_ALIAS_BM = "bm-node";

    private static final String APP_IMAGE_NAME = "backend:latest";
    private static final String APP_PORT = "8080";

    private static final String FRONTEND_USER = "superuser";
    private static final String FRONTEND_PASS = "su1234";

    // 1. PostgreSQL Container
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.2")
            .withNetwork(network)
            .withNetworkAliases(NETWORK_ALIAS_DB); // Erlaubt Zugriff über "postgres-db"

    // 2. Cluster Manager (CM) Container
    @Container
    private static final GenericContainer<?> cmContainer = new GenericContainer<>(APP_IMAGE_NAME)
            .withNetwork(network) // WICHTIG: Im gemeinsamen Netzwerk
            .withExposedPorts(Integer.parseInt(APP_PORT))
            .withNetworkAliases(NETWORK_ALIAS_CM)
            .dependsOn(postgres)
            .withEnv("SPRING_PROFILES_ACTIVE", "dev,cluster_manager")
            .withEnv("SPRING_SECURITY_USER_NAME", FRONTEND_USER)
            .withEnv("SPRING_SECURITY_USER_PASSWORD", FRONTEND_PASS)

            // Korrekte DB Konfiguration über Spring Boot Env Vars
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://" + NETWORK_ALIAS_DB + ":5432/" + postgres.getDatabaseName())
            .withEnv("SPRING_DATASOURCE_USERNAME", postgres.getUsername())
            .withEnv("SPRING_DATASOURCE_PASSWORD", postgres.getPassword())

            // Konfiguration für interne Kommunikation
            .withEnv("CM_ADDRESS", NETWORK_ALIAS_CM + ":" + APP_PORT)
            .withEnv("NODE_ADDRESS", NETWORK_ALIAS_CM + ":" + APP_PORT)
            .withEnv("BM_ADDRESS", NETWORK_ALIAS_BM + ":" + APP_PORT)
            .waitingFor(Wait.forHttp("/api/v1/ping") // Bekannter, freigegebener Endpunkt
                    .forPort(Integer.parseInt(APP_PORT))
                    .forStatusCode(200) // Erwarte 200 OK
                    .withStartupTimeout(Duration.ofSeconds(60)));


    // 3. Backup Manager (BM) Container
    @Container
    private static final GenericContainer<?> bmContainer = new GenericContainer<>(APP_IMAGE_NAME)
            .withNetwork(network) // WICHTIG: Im gemeinsamen Netzwerk
            .withExposedPorts(Integer.parseInt(APP_PORT))
            .withNetworkAliases(NETWORK_ALIAS_BM)
            .dependsOn(postgres, cmContainer)
            .withEnv("SPRING_PROFILES_ACTIVE", "dev,backup_manager")

            // Korrekte DB Konfiguration über Spring Boot Env Vars
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://" + NETWORK_ALIAS_DB + ":5432/" + postgres.getDatabaseName())
            .withEnv("SPRING_DATASOURCE_USERNAME", postgres.getUsername())
            .withEnv("SPRING_DATASOURCE_PASSWORD", postgres.getPassword())

            // Crucial: Zeigt auf den CM Container im gemeinsamen Netzwerk
            .withEnv("CM_ADDRESS", NETWORK_ALIAS_CM + ":" + APP_PORT)
            .withEnv("NODE_ADDRESS", NETWORK_ALIAS_BM + ":" + APP_PORT)
            .withEnv("BM_ADDRESS", NETWORK_ALIAS_BM + ":" + APP_PORT)

            // Warte auf die erfolgreiche Registrierung
            .waitingFor(Wait.forLogMessage(".*Successfully registered node with CM.*", 1)
                    .withStartupTimeout(Duration.ofSeconds(90))); // Höheres Timeout für das gesamte Setup


    /**
     * E2E Test: Verifies that the BM node successfully registers itself with the CM node
     * and that the CM node acknowledges the registration.
     */
    @Test
    void backupManager_registers_and_cm_lists_it() {
        // ACT: The registration and sync happened during the container startup (in waitingFor)

        // ASSERT: Query the CM's API (ClusterManagerController.getNodes) to verify the BM is present.

        // 1. Get the real external address for the CM
        String cmHost = cmContainer.getHost();
        Integer cmPort = cmContainer.getMappedPort(Integer.parseInt(APP_PORT));
        String cmBaseUrl = String.format("http://%s:%d", cmHost, cmPort);

        // 2. Use a simple HTTP client to query the CM's API
        TestRestTemplate restTemplate = new TestRestTemplate(FRONTEND_USER, FRONTEND_PASS);
        String nodesEndpoint = cmBaseUrl + "/api/v1/cm/nodes";

        // 3. Make the call
        ResponseEntity<String> response = restTemplate.getForEntity(nodesEndpoint, String.class);

        // Final Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check the response body for the BM's network address
        assertThat(response.getBody()).contains(NETWORK_ALIAS_BM + ":" + APP_PORT);

        String cmLogs = cmContainer.getLogs();

        // Dieser Assert prüft, ob der Rückkanal (Sync) funktioniert hat
        assertThat(cmLogs)
                .as("CM should successfully push tables to BM via /api/v1/sync without Auth error")
                .contains("Pushed tables to " + NETWORK_ALIAS_BM + ":" + APP_PORT);
    }
}