package com.kenect.api_aggregator.mapper;

import com.kenect.api_aggregator.dto.ExternalContactDto;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.model.ContactSource;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public Contact toContact(ExternalContactDto externalContact, ContactSource source) {
        if (externalContact == null) {
            return null;
        }

        return Contact.builder()
                .id(externalContact.getId())
                .name(externalContact.getName())
                .email(externalContact.getEmail())
                .source(source.getValue())
                .createdAt(externalContact.getCreatedAt())
                .updatedAt(externalContact.getUpdatedAt())
                .build();
    }
}
