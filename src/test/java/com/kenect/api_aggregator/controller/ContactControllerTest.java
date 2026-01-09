package com.kenect.api_aggregator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kenect.api_aggregator.dto.ContactQueryParams;
import com.kenect.api_aggregator.dto.PaginatedResponse;
import com.kenect.api_aggregator.exception.ExternalApiException;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.model.ContactSource;
import com.kenect.api_aggregator.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactService contactService;

    @Test
    void getAllContacts_ShouldReturnFlatList_WhenNoParametersProvided() throws Exception {
        Instant now = Instant.parse("2020-06-24T19:37:16.688Z");

        Contact contact1 = Contact.builder()
                .id(1L)
                .name("Mrs. Willian Bradtke")
                .email("jerold@example.net")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Contact contact2 = Contact.builder()
                .id(2L)
                .name("John Doe")
                .email("johndoe@example.net")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(contactService.getAllContacts(isNull())).thenReturn(List.of(contact1, contact2));

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Mrs. Willian Bradtke"))
                .andExpect(jsonPath("$[0].email").value("jerold@example.net"))
                .andExpect(jsonPath("$[0].source").value("KENECT_LABS"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("John Doe"))
                .andExpect(jsonPath("$[1].email").value("johndoe@example.net"))
                .andExpect(jsonPath("$[1].source").value("KENECT_LABS"));
    }

    @Test
    void getAllContacts_ShouldReturnEmptyFlatList_WhenNoContactsExist() throws Exception {
        when(contactService.getAllContacts(isNull())).thenReturn(List.of());

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllContacts_ShouldReturnBadGateway_WhenExternalApiException() throws Exception {
        when(contactService.getAllContacts(isNull()))
                .thenThrow(new ExternalApiException("External API is unavailable"));

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Failed to retrieve data from external service"));
    }

    @Test
    void getAllContacts_ShouldReturnInternalServerError_WhenUnexpectedException() throws Exception {
        when(contactService.getAllContacts(isNull()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void getAllContacts_ShouldReturnPaginatedResponse_WhenPageParameterProvided() throws Exception {
        Instant now = Instant.parse("2020-06-24T19:37:16.688Z");

        Contact contact = Contact.builder()
                .id(1L)
                .name("Mrs. Willian Bradtke")
                .email("jerold@example.net")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PaginatedResponse<Contact> response = PaginatedResponse.<Contact>builder()
                .content(List.of(contact))
                .page(1)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(contactService.getContactsPaginated(any(ContactQueryParams.class))).thenReturn(response);

        mockMvc.perform(get("/contacts")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(header().string("X-Current-Page", "1"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllContacts_ShouldReturnPaginatedResponse_WhenSizeParameterProvided() throws Exception {
        Instant now = Instant.parse("2020-06-24T19:37:16.688Z");

        Contact contact = Contact.builder()
                .id(1L)
                .name("Mrs. Willian Bradtke")
                .email("jerold@example.net")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PaginatedResponse<Contact> response = PaginatedResponse.<Contact>builder()
                .content(List.of(contact))
                .page(1)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(contactService.getContactsPaginated(any(ContactQueryParams.class))).thenReturn(response);

        mockMvc.perform(get("/contacts")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getAllContacts_ShouldHandleCustomPageAndSize() throws Exception {
        Instant now = Instant.parse("2020-06-24T19:37:16.688Z");

        Contact contact = Contact.builder()
                .id(1L)
                .name("Mrs. Willian Bradtke")
                .email("jerold@example.net")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PaginatedResponse<Contact> response = PaginatedResponse.<Contact>builder()
                .content(List.of(contact))
                .page(2)
                .size(10)
                .totalElements(25)
                .totalPages(3)
                .hasNext(true)
                .hasPrevious(true)
                .isFirst(false)
                .isLast(false)
                .build();

        when(contactService.getContactsPaginated(any(ContactQueryParams.class))).thenReturn(response);

        mockMvc.perform(get("/contacts")
                        .param("page", "2")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    @Test
    void getAllContacts_ShouldFilterBySource_WhenSourceParameterProvided() throws Exception {
        Instant now = Instant.parse("2020-06-24T19:37:16.688Z");

        Contact contact = Contact.builder()
                .id(1L)
                .name("Mrs. Willian Bradtke")
                .email("jerold@example.net")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(contactService.getAllContacts(ContactSource.KENECT_LABS)).thenReturn(List.of(contact));

        mockMvc.perform(get("/contacts")
                        .param("source", "KENECT_LABS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].source").value("KENECT_LABS"));
    }

    @Test
    void getAllContacts_ShouldFilterBySourceInPagination_WhenBothProvided() throws Exception {
        Instant now = Instant.parse("2020-06-24T19:37:16.688Z");

        Contact contact = Contact.builder()
                .id(1L)
                .name("Mrs. Willian Bradtke")
                .email("jerold@example.net")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PaginatedResponse<Contact> response = PaginatedResponse.<Contact>builder()
                .content(List.of(contact))
                .page(1)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(contactService.getContactsPaginated(any(ContactQueryParams.class))).thenReturn(response);

        mockMvc.perform(get("/contacts")
                        .param("page", "1")
                        .param("source", "KENECT_LABS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].source").value("KENECT_LABS"));
    }
}
