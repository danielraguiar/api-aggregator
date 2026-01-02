package com.kenect.api_aggregator.mapper;

import com.kenect.api_aggregator.dto.ExternalContactDto;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.model.ContactSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ContactMapperTest {

    private ContactMapper contactMapper;

    @BeforeEach
    void setUp() {
        contactMapper = new ContactMapper();
    }

    @Test
    void toContact_ShouldMapAllFields_WhenValidDto() {
        Instant createdAt = Instant.parse("2020-06-24T19:37:16.688Z");
        Instant updatedAt = Instant.parse("2020-06-24T19:37:16.688Z");

        ExternalContactDto dto = ExternalContactDto.builder()
                .id(1L)
                .name("Mrs. Willian Bradtke")
                .email("jerold@example.net")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Contact contact = contactMapper.toContact(dto, ContactSource.KENECT_LABS);

        assertNotNull(contact);
        assertEquals(1L, contact.getId());
        assertEquals("Mrs. Willian Bradtke", contact.getName());
        assertEquals("jerold@example.net", contact.getEmail());
        assertEquals("KENECT_LABS", contact.getSource());
        assertEquals(createdAt, contact.getCreatedAt());
        assertEquals(updatedAt, contact.getUpdatedAt());
    }

    @Test
    void toContact_ShouldReturnNull_WhenDtoIsNull() {
        Contact contact = contactMapper.toContact(null, ContactSource.KENECT_LABS);
        assertNull(contact);
    }

    @Test
    void toContact_ShouldHandleNullFields_WhenDtoHasNullValues() {
        ExternalContactDto dto = ExternalContactDto.builder()
                .id(null)
                .name(null)
                .email(null)
                .createdAt(null)
                .updatedAt(null)
                .build();

        Contact contact = contactMapper.toContact(dto, ContactSource.KENECT_LABS);

        assertNotNull(contact);
        assertNull(contact.getId());
        assertNull(contact.getName());
        assertNull(contact.getEmail());
        assertEquals("KENECT_LABS", contact.getSource());
        assertNull(contact.getCreatedAt());
        assertNull(contact.getUpdatedAt());
    }
}
