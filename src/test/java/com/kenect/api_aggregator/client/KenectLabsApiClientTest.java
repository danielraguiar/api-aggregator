package com.kenect.api_aggregator.client;

import com.kenect.api_aggregator.dto.ExternalContactDto;
import com.kenect.api_aggregator.dto.ExternalContactResponse;
import com.kenect.api_aggregator.dto.PaginationMetadata;
import com.kenect.api_aggregator.exception.ExternalApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class KenectLabsApiClientTest {

    private MockWebServer mockWebServer;
    private KenectLabsApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        apiClient = new KenectLabsApiClient(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchContactsPage_ShouldReturnContacts_WhenSuccessfulResponse() throws InterruptedException {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "name": "John Doe",
                        "email": "john@example.com",
                        "created_at": "2020-06-24T19:37:16.688Z",
                        "updated_at": "2020-06-24T19:37:16.688Z"
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
                .addHeader("Total-Count", "1")
                .addHeader("Link", "<https://example.com/api/v1/contacts?page=1>; rel=\"first\""));

        ExternalContactResponse response = apiClient.fetchContactsPage(1);

        assertNotNull(response);
        assertNotNull(response.getContacts());
        assertEquals(1, response.getContacts().size());

        ExternalContactDto contact = response.getContacts().get(0);
        assertEquals(1L, contact.getId());
        assertEquals("John Doe", contact.getName());
        assertEquals("john@example.com", contact.getEmail());

        PaginationMetadata pagination = response.getPagination();
        assertNotNull(pagination);
        assertEquals(1, pagination.getCurrentPage());
        assertEquals(20, pagination.getPageItems());
        assertEquals(1, pagination.getTotalPages());
        assertEquals(1, pagination.getTotalCount());
        assertFalse(pagination.hasNextPage());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/api/v1/contacts?page=1", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void fetchContactsPage_ShouldParsePaginationCorrectly_WhenMultiplePages() {
        String responseBody = """
                [
                    {
                        "id": 1,
                        "name": "John Doe",
                        "email": "john@example.com",
                        "created_at": "2020-06-24T19:37:16.688Z",
                        "updated_at": "2020-06-24T19:37:16.688Z"
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Current-Page", "2")
                .addHeader("Page-Items", "20")
                .addHeader("Total-Pages", "5")
                .addHeader("Total-Count", "100")
                .addHeader("Link", "<https://example.com?page=1>; rel=\"first\", " +
                        "<https://example.com?page=1>; rel=\"prev\", " +
                        "<https://example.com?page=3>; rel=\"next\", " +
                        "<https://example.com?page=5>; rel=\"last\""));

        ExternalContactResponse response = apiClient.fetchContactsPage(2);

        PaginationMetadata pagination = response.getPagination();
        assertNotNull(pagination);
        assertEquals(2, pagination.getCurrentPage());
        assertEquals(5, pagination.getTotalPages());
        assertTrue(pagination.hasNextPage());
        assertEquals("https://example.com?page=1", pagination.getFirstPageUrl());
        assertEquals("https://example.com?page=1", pagination.getPrevPageUrl());
        assertEquals("https://example.com?page=3", pagination.getNextPageUrl());
        assertEquals("https://example.com?page=5", pagination.getLastPageUrl());
    }

    @Test
    void fetchContactsPage_ShouldThrowException_WhenServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        assertThrows(ExternalApiException.class, () -> apiClient.fetchContactsPage(1));
    }

    @Test
    void fetchContactsPage_ShouldThrowException_WhenUnauthorized() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        assertThrows(ExternalApiException.class, () -> apiClient.fetchContactsPage(1));
    }

    @Test
    void fetchContactsPage_ShouldHandleEmptyResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("[]")
                .addHeader("Content-Type", "application/json")
                .addHeader("Current-Page", "1")
                .addHeader("Total-Pages", "1"));

        ExternalContactResponse response = apiClient.fetchContactsPage(1);

        assertNotNull(response);
        assertNotNull(response.getContacts());
        assertTrue(response.getContacts().isEmpty());
    }
}
