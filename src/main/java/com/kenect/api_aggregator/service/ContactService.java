package com.kenect.api_aggregator.service;

import com.kenect.api_aggregator.client.KenectLabsApiClient;
import com.kenect.api_aggregator.dto.ExternalContactResponse;
import com.kenect.api_aggregator.mapper.ContactMapper;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.model.ContactSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ContactService {

    private final KenectLabsApiClient apiClient;
    private final ContactMapper contactMapper;

    public ContactService(KenectLabsApiClient apiClient, ContactMapper contactMapper) {
        this.apiClient = apiClient;
        this.contactMapper = contactMapper;
    }

    public List<Contact> getAllContacts() {
        log.info("Starting to fetch all contacts from external API");
        long startTime = System.currentTimeMillis();

        List<Contact> allContacts = new ArrayList<>();
        int currentPage = 1;
        boolean hasMorePages = true;

        while (hasMorePages) {
            ExternalContactResponse response = apiClient.fetchContactsPage(currentPage);

            if (response.getContacts() != null && !response.getContacts().isEmpty()) {
                List<Contact> pageContacts = response.getContacts().stream()
                        .map(dto -> contactMapper.toContact(dto, ContactSource.KENECT_LABS))
                        .toList();

                allContacts.addAll(pageContacts);
                log.debug("Added {} contacts from page {}", pageContacts.size(), currentPage);
            }

            hasMorePages = response.getPagination() != null && response.getPagination().hasNextPage();
            currentPage++;
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully fetched {} contacts in {} ms", allContacts.size(), duration);

        return allContacts;
    }
}
