package com.bcm.cluster_manager.config.security;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.time.Instant;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Disabled("Skipping Spring context startup for now")
class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("bcm_test")
                    .withUsername("bcm")
                    .withPassword("bcm");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url",
                () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Qualifier("userMapperBN")
    @Autowired
    UserMapper userRepository;

    @Qualifier("groupMapperBN")
    @Autowired
    GroupMapper groupRepository;

    @Qualifier("userGroupRelationMapperBN")
    @Autowired
    UserGroupRelationMapper userGroupRelationRepository;

    @BeforeEach
    void setup() {
        Mono<Void> init =
                Mono.defer(() -> {
                    User user = new User();
                    user.setName("testuser");
                    user.setPasswordHash("secret");
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

