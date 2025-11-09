package test.com.bcm.shared;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NodeControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testEndpointContainsString() {
        String response = restTemplate.getForObject("/example", String.class);
        assert response.equals("Here is a string");
    }
}
