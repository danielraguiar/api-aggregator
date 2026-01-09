package com.kenect.api_aggregator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kenect.api_aggregator.dto.PaginatedResponse;
import com.kenect.api_aggregator.exception.ExternalApiException;
import com.kenect.api_aggregator.model.Contact;
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
import static org.mockito.ArgumentMatchers.anyInt;
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
    void getAllContacts_ShouldReturnContactList_WhenContactsExist() throws Exception {
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

        PaginatedResponse<Contact> response = PaginatedResponse.<Contact>builder()
                .content(List.of(contact1, contact2))
                .page(1)
                .size(20)
                .totalElements(2)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(contactService.getContactsPaginated(1, 20)).thenReturn(response);

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(header().string("X-Current-Page", "1"))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Mrs. Willian Bradtke"))
                .andExpect(jsonPath("$.content[0].email").value("jerold@example.net"))
                .andExpect(jsonPath("$.content[0].source").value("KENECT_LABS"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].email").value("johndoe@example.net"))
                .andExpect(jsonPath("$.content[1].source").value("KENECT_LABS"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void getAllContacts_ShouldReturnEmptyList_WhenNoContactsExist() throws Exception {
        PaginatedResponse<Contact> response = PaginatedResponse.<Contact>builder()
                .content(List.of())
                .page(1)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(contactService.getContactsPaginated(1, 20)).thenReturn(response);

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllContacts_ShouldReturnBadGateway_WhenExternalApiException() throws Exception {
        when(contactService.getContactsPaginated(anyInt(), anyInt()))
                .thenThrow(new ExternalApiException("External API is unavailable"));

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Failed to retrieve data from external service"));
    }

    @Test
    void getAllContacts_ShouldReturnInternalServerError_WhenUnexpectedException() throws Exception {
        when(contactService.getContactsPaginated(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
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

        when(contactService.getContactsPaginated(2, 10)).thenReturn(response);

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
    void getAllContacts_ShouldReturnBadRequest_WhenInvalidPageParameter() throws Exception {
        when(contactService.getContactsPaginated(0, 20))
                .thenThrow(new IllegalArgumentException("Page number must be greater than 0"));

        mockMvc.perform(get("/contacts")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }
}
