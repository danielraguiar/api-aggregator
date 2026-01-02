package com.kenect.api_aggregator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

        when(contactService.getAllContacts()).thenReturn(List.of(contact1, contact2));

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
    void getAllContacts_ShouldReturnEmptyList_WhenNoContactsExist() throws Exception {
        when(contactService.getAllContacts()).thenReturn(List.of());

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllContacts_ShouldReturnBadGateway_WhenExternalApiException() throws Exception {
        when(contactService.getAllContacts())
                .thenThrow(new ExternalApiException("External API is unavailable"));

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Failed to retrieve data from external service"));
    }

    @Test
    void getAllContacts_ShouldReturnInternalServerError_WhenUnexpectedException() throws Exception {
        when(contactService.getAllContacts())
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/contacts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
