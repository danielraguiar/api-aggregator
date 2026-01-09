package com.kenect.api_aggregator.integration;

import com.kenect.api_aggregator.dto.PaginatedResponse;
import com.kenect.api_aggregator.model.Contact;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContactIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        registry.add("kenect.api.base-url", () -> mockWebServer.url("/").toString());
        registry.add("kenect.api.bearer-token", () -> "test-token");
    }

    @BeforeEach
    void setUp() {
        while (mockWebServer.getRequestCount() > 0) {
            try {
                mockWebServer.takeRequest();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void getContacts_ShouldReturnFlatList_WhenNoParametersProvided() {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "name": "Mrs. Willian Bradtke",
                        "email": "jerold@example.net",
                        "created_at": "2020-06-24T19:37:16.688Z",
                        "updated_at": "2020-06-24T19:37:16.688Z"
                    },
                    {
                        "id": 2,
                        "name": "John Doe",
                        "email": "johndoe@example.net",
                        "created_at": "2021-02-10T11:10:09.987Z",
                        "updated_at": "2022-05-05T15:27:17.547Z"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Current-Page", "1")
                .addHeader("Page-Items", "20")
                .addHeader("Total-Pages", "1")
                .addHeader("Total-Count", "2"));

        ResponseEntity<List<Contact>> response = restTemplate.exchange(
                "/contacts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Contact>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        Contact contact1 = response.getBody().get(0);
        assertEquals(1L, contact1.getId());
        assertEquals("Mrs. Willian Bradtke", contact1.getName());
        assertEquals("jerold@example.net", contact1.getEmail());
        assertEquals("KENECT_LABS", contact1.getSource());
        assertNotNull(contact1.getCreatedAt());
        assertNotNull(contact1.getUpdatedAt());

        Contact contact2 = response.getBody().get(1);
        assertEquals(2L, contact2.getId());
        assertEquals("John Doe", contact2.getName());
        assertEquals("johndoe@example.net", contact2.getEmail());
        assertEquals("KENECT_LABS", contact2.getSource());
    }

    @Test
    void getContacts_ShouldAggregateMultiplePages_WhenPaginationExists() {
        String page1Response = """
                [
                    {
                        "id": 1,
                        "name": "Contact 1",
                        "email": "contact1@example.net",
                        "created_at": "2020-06-24T19:37:16.688Z",
                        "updated_at": "2020-06-24T19:37:16.688Z"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(page1Response)
                .addHeader("Content-Type", "application/json")
                .addHeader("Current-Page", "1")
                .addHeader("Page-Items", "1")
                .addHeader("Total-Pages", "2")
                .addHeader("Total-Count", "2")
                .addHeader("Link", "<https://example.com?page=2>; rel=\"next\""));

        String page2Response = """
                [
                    {
                        "id": 2,
                        "name": "Contact 2",
                        "email": "contact2@example.net",
                        "created_at": "2021-02-10T11:10:09.987Z",
                        "updated_at": "2022-05-05T15:27:17.547Z"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(page2Response)
                .addHeader("Content-Type", "application/json")
                .addHeader("Current-Page", "2")
                .addHeader("Page-Items", "1")
                .addHeader("Total-Pages", "2")
                .addHeader("Total-Count", "2"));

        ResponseEntity<List<Contact>> response = restTemplate.exchange(
                "/contacts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Contact>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Contact 1", response.getBody().get(0).getName());
        assertEquals("Contact 2", response.getBody().get(1).getName());
    }

    @Test
    void getContacts_ShouldReturnEmptyList_WhenNoContacts() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
                .addHeader("Current-Page", "1")
                .addHeader("Total-Pages", "1")
                .addHeader("Total-Count", "0"));

        ResponseEntity<List<Contact>> response = restTemplate.exchange(
                "/contacts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Contact>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getContacts_ShouldReturnPaginatedResponse_WhenParametersProvided() {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "name": "Mrs. Willian Bradtke",
                        "email": "jerold@example.net",
                        "created_at": "2020-06-24T19:37:16.688Z",
                        "updated_at": "2020-06-24T19:37:16.688Z"
                    },
                    {
                        "id": 2,
                        "name": "John Doe",
                        "email": "johndoe@example.net",
                        "created_at": "2021-02-10T11:10:09.987Z",
                        "updated_at": "2022-05-05T15:27:17.547Z"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Current-Page", "1")
                .addHeader("Page-Items", "20")
                .addHeader("Total-Pages", "1")
                .addHeader("Total-Count", "2"));

        ResponseEntity<PaginatedResponse<Contact>> response = restTemplate.exchange(
                "/contacts?page=1&size=20",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponse<Contact>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(1, response.getBody().getPage());
        assertEquals(20, response.getBody().getSize());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());
    }

    @Test
    void getContacts_ShouldReturnBadGateway_WhenExternalApiFailure() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        ResponseEntity<String> response = restTemplate.getForEntity("/contacts", String.class);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Failed to retrieve data from external service"));
    }
}
