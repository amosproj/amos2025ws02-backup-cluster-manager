package com.bcm.cluster_manager.config.security;

import com.bcm.shared.model.database.Group;
import com.bcm.shared.model.database.User;
import com.bcm.shared.model.database.UserGroupRelation;
import com.bcm.shared.repository.GroupMapper;
import com.bcm.shared.repository.UserGroupRelationMapper;
import com.bcm.shared.repository.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.time.Instant;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("bcm_test")
                    .withUsername("bcm")
                    .withPassword("bcm");

    @DynamicPropertySource
    static void registerDataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    MockMvc mockMvc;

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
        User user = new User();
        user.setName("testuser");
        user.setPasswordHash(passwordEncoder.encode("secret"));
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userRepository.insert(user);
        Long userId = user.getId();

        Group group = groupRepository.findById(3L);
        Long groupId = group.getId();

        var ugr = new UserGroupRelation();
        ugr.setUserId(userId);
        ugr.setGroupId(groupId);
        ugr.setAddedAt(Instant.now());
        userGroupRelationRepository.insert(ugr);
    }

    @Test
    void logged_in_can_access_users() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/cm/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"username":"testuser","password":"secret"}
            """))
                .andExpect(status().isOk()).andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session, "Session must not be null after login");
    }


}

