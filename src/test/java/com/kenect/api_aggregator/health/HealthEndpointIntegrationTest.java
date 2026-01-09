package com.kenect.api_aggregator.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void actuatorHealth_ShouldReturnHealthStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("status"));
    }

    @Test
    void customHealth_ShouldReturnHealthStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("UP"));
        assertTrue(response.getBody().contains("api-aggregator"));
    }
}
