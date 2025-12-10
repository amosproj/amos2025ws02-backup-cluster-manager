package com.bcm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Skipping Spring context startup for now")
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
