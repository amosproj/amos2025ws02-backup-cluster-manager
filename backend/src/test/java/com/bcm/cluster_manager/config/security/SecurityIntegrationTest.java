package com.bcm.cluster_manager.config.security;

import com.bcm.cluster_manager.service.SyncService;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import com.bcm.shared.service.NodeHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "CM_ADDRESS=http://localhost:8080")
@ActiveProfiles({"test", "cluster_manager"})
@Testcontainers
class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("postgres")
            .withUsername("appuser")
            .withPassword("apppassword");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
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
    @MockitoBean
    private NodeHttpClient nodeHttpClient;

    @MockitoBean
    private SyncService syncService;

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserMapper userRepository;

    @Autowired
    GroupMapper groupRepository;

    @Autowired
    UserGroupRelationMapper userGroupRelationRepository;

    @BeforeEach
    void setup() {
        Mono<Void> init =
                Mono.defer(() -> {
                    User user = new User();
                    user.setName("testuser");
                    user.setPasswordHash(passwordEncoder.encode("secret"));
                    user.setEnabled(true);
                    user.setCreatedAt(Instant.now());
                    user.setUpdatedAt(Instant.now());

                    return userRepository.save(user)
                            .flatMap(savedUser ->
                                    groupRepository.findById(3L)
                                            .flatMap(group -> {
                                                UserGroupRelation ugr = new UserGroupRelation();
                                                ugr.setUserId(savedUser.getId());
                                                ugr.setGroupId(group.getId());
                                                ugr.setAddedAt(Instant.now());
                                                return userGroupRelationRepository.insert(ugr);
                                            })
                            )
                            .then();
                });

        init.block();
    }

    @Test
    void logged_in_can_access_users() {
        var loginResult = webTestClient.post()
                .uri("/api/v1/cm/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"username":"testuser","password":"secret"}
                """)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class);

        String setCookie = loginResult.getResponseHeaders().getFirst("Set-Cookie");
        assertThat(setCookie).isNotBlank();

        // Use cookie on next request (example: /users)
        webTestClient.get()
                .uri("/api/v1/cm/users")
                .header("Cookie", setCookie)
                .exchange()
                .expectStatus().isOk();
    }


}

