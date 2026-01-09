package com.kenect.api_aggregator.service;

import com.kenect.api_aggregator.client.KenectLabsApiClient;
import com.kenect.api_aggregator.dto.ExternalContactDto;
import com.kenect.api_aggregator.dto.ExternalContactResponse;
import com.kenect.api_aggregator.dto.PaginationMetadata;
import com.kenect.api_aggregator.mapper.ContactMapper;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.model.ContactSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class ContactServiceCacheTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private KenectLabsApiClient apiClient;

    @MockBean
    private ContactMapper contactMapper;

    @Test
    void getAllContacts_ShouldCacheResults() {
        Instant now = Instant.now();

        ExternalContactDto externalContactDto = ExternalContactDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Contact contact = Contact.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(1)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto))
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(1)).thenReturn(response);
        when(contactMapper.toContact(eq(externalContactDto), any(ContactSource.class))).thenReturn(contact);

        List<Contact> firstCall = contactService.getAllContacts(null);
        List<Contact> secondCall = contactService.getAllContacts(null);
        List<Contact> thirdCall = contactService.getAllContacts(null);

        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertNotNull(thirdCall);
        assertEquals(1, firstCall.size());
        assertEquals(1, secondCall.size());
        assertEquals(1, thirdCall.size());

        verify(apiClient, times(1)).fetchContactsPage(1);
        verify(contactMapper, times(1)).toContact(any(), any());
    }

    @Test
    void evictContactsCache_ShouldClearCache() {
        Instant now = Instant.now();

        ExternalContactDto externalContactDto = ExternalContactDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Contact contact = Contact.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(1)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto))
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(1)).thenReturn(response);
        when(contactMapper.toContact(eq(externalContactDto), any(ContactSource.class))).thenReturn(contact);

        contactService.getAllContacts(null);

        verify(apiClient, times(1)).fetchContactsPage(1);

        contactService.evictContactsCache();

        contactService.getAllContacts(null);

        verify(apiClient, times(2)).fetchContactsPage(1);
    }

    @Test
    void cacheManager_ShouldBeConfigured() {
        assertNotNull(cacheManager);
        assertNotNull(cacheManager.getCache("contacts"));
    }
}
