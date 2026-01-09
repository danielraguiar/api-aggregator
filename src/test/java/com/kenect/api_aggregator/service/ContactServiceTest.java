package com.kenect.api_aggregator.service;

import com.kenect.api_aggregator.client.KenectLabsApiClient;
import com.kenect.api_aggregator.dto.ContactQueryParams;
import com.kenect.api_aggregator.dto.ExternalContactDto;
import com.kenect.api_aggregator.dto.ExternalContactResponse;
import com.kenect.api_aggregator.dto.PaginatedResponse;
import com.kenect.api_aggregator.dto.PaginationMetadata;
import com.kenect.api_aggregator.mapper.ContactMapper;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.model.ContactSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private KenectLabsApiClient apiClient;

    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private ContactService contactService;

    private ExternalContactDto externalContactDto1;
    private ExternalContactDto externalContactDto2;
    private Contact contact1;
    private Contact contact2;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        externalContactDto1 = ExternalContactDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .createdAt(now)
                .updatedAt(now)
                .build();

        externalContactDto2 = ExternalContactDto.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@example.com")
                .createdAt(now)
                .updatedAt(now)
                .build();

        contact1 = Contact.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();

        contact2 = Contact.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@example.com")
                .source("KENECT_LABS")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void getAllContacts_ShouldReturnAllContacts_WhenSinglePage() {
        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(2)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto1, externalContactDto2))
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(1)).thenReturn(response);
        when(contactMapper.toContact(eq(externalContactDto1), any())).thenReturn(contact1);
        when(contactMapper.toContact(eq(externalContactDto2), any())).thenReturn(contact2);

        List<Contact> result = contactService.getAllContacts(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
        verify(apiClient, times(1)).fetchContactsPage(1);
        verify(contactMapper, times(2)).toContact(any(), any());
    }

    @Test
    void getAllContacts_ShouldFilterBySource_WhenSourceProvided() {
        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(2)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto1, externalContactDto2))
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(1)).thenReturn(response);
        when(contactMapper.toContact(eq(externalContactDto1), any())).thenReturn(contact1);
        when(contactMapper.toContact(eq(externalContactDto2), any())).thenReturn(contact2);

        List<Contact> result = contactService.getAllContacts(ContactSource.KENECT_LABS);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(apiClient, times(1)).fetchContactsPage(1);
    }

    @Test
    void getAllContacts_ShouldHandleMultiplePages_WhenPaginationExists() {
        PaginationMetadata page1Pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(2)
                .totalCount(2)
                .build();

        PaginationMetadata page2Pagination = PaginationMetadata.builder()
                .currentPage(2)
                .totalPages(2)
                .totalCount(2)
                .build();

        ExternalContactResponse page1Response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto1))
                .pagination(page1Pagination)
                .build();

        ExternalContactResponse page2Response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto2))
                .pagination(page2Pagination)
                .build();

        when(apiClient.fetchContactsPage(1)).thenReturn(page1Response);
        when(apiClient.fetchContactsPage(2)).thenReturn(page2Response);
        when(contactMapper.toContact(eq(externalContactDto1), any())).thenReturn(contact1);
        when(contactMapper.toContact(eq(externalContactDto2), any())).thenReturn(contact2);

        List<Contact> result = contactService.getAllContacts(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(apiClient, times(1)).fetchContactsPage(1);
        verify(apiClient, times(1)).fetchContactsPage(2);
        verify(contactMapper, times(2)).toContact(any(), any());
    }

    @Test
    void getAllContacts_ShouldReturnEmptyList_WhenNoContactsAvailable() {
        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(0)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of())
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(1)).thenReturn(response);

        List<Contact> result = contactService.getAllContacts(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(apiClient, times(1)).fetchContactsPage(1);
        verify(contactMapper, never()).toContact(any(), any());
    }

    @Test
    void getContactsPaginated_ShouldReturnFirstPage() {
        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(2)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto1, externalContactDto2))
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(anyInt())).thenReturn(response);
        when(contactMapper.toContact(any(), any())).thenReturn(contact1, contact2);

        ContactQueryParams params = ContactQueryParams.builder()
                .page(1)
                .size(20)
                .build();

        PaginatedResponse<Contact> result = contactService.getContactsPaginated(params);

        assertNotNull(result);
        assertEquals(1, result.getPage());
        assertEquals(20, result.getSize());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.isHasNext());
        assertFalse(result.isHasPrevious());
    }

    @Test
    void getContactsPaginated_ShouldFilterBySource() {
        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(2)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto1, externalContactDto2))
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(anyInt())).thenReturn(response);
        when(contactMapper.toContact(any(), any())).thenReturn(contact1, contact2);

        ContactQueryParams params = ContactQueryParams.builder()
                .page(1)
                .size(20)
                .source(ContactSource.KENECT_LABS)
                .build();

        PaginatedResponse<Contact> result = contactService.getContactsPaginated(params);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void getContactsPaginated_ShouldUseDefaultValues_WhenParamsNotProvided() {
        PaginationMetadata pagination = PaginationMetadata.builder()
                .currentPage(1)
                .totalPages(1)
                .totalCount(2)
                .build();

        ExternalContactResponse response = ExternalContactResponse.builder()
                .contacts(List.of(externalContactDto1, externalContactDto2))
                .pagination(pagination)
                .build();

        when(apiClient.fetchContactsPage(anyInt())).thenReturn(response);
        when(contactMapper.toContact(any(), any())).thenReturn(contact1, contact2);

        ContactQueryParams params = ContactQueryParams.builder().build();

        PaginatedResponse<Contact> result = contactService.getContactsPaginated(params);

        assertNotNull(result);
        assertEquals(1, result.getPage());
        assertEquals(20, result.getSize());
    }
}
