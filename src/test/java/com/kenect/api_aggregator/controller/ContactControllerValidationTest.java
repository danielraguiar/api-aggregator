package com.kenect.api_aggregator.controller;

import com.kenect.api_aggregator.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenPageIsZero() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.details").value("Page number must be greater than 0"));
    }

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("page", "-5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.details").value("Page number must be greater than 0"));
    }

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.details").value("Page size must be at least 1"));
    }

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenSizeExceedsLimit() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("size", "150")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"))
                .andExpect(jsonPath("$.details").value("Page size must not exceed 100"));
    }

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenSourceIsInvalid() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("source", "INVALID_SOURCE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"))
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("KENECT_LABS")));
    }

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenPageIsNotNumeric() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("page", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenSizeIsNotNumeric() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("size", "xyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid parameter type"));
    }

    @Test
    void getAllContacts_ShouldReturnBadRequest_WhenMultipleValidationErrors() throws Exception {
        mockMvc.perform(get("/contacts")
                        .param("page", "0")
                        .param("size", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameters"));
    }
}
