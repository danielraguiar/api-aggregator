package com.kenect.api_aggregator.service;

import com.kenect.api_aggregator.client.KenectLabsApiClient;
import com.kenect.api_aggregator.dto.ExternalContactResponse;
import com.kenect.api_aggregator.dto.PaginatedResponse;
import com.kenect.api_aggregator.mapper.ContactMapper;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.model.ContactSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.kenect.api_aggregator.config.CacheConfig.CONTACTS_CACHE;

@Slf4j
@Service
public class ContactService {

    private final KenectLabsApiClient apiClient;
    private final ContactMapper contactMapper;

    public ContactService(KenectLabsApiClient apiClient, ContactMapper contactMapper) {
        this.apiClient = apiClient;
        this.contactMapper = contactMapper;
    }

    @Cacheable(value = CONTACTS_CACHE, key = "'all'")
    public List<Contact> getAllContacts() {
        log.info("Cache miss - Starting to fetch all contacts from external API");
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

    public PaginatedResponse<Contact> getContactsPaginated(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        log.info("Fetching paginated contacts - page: {}, size: {}", page, size);
        
        List<Contact> allContacts = getAllContacts();
        
        int totalElements = allContacts.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<Contact> pageContent = startIndex < totalElements 
                ? allContacts.subList(startIndex, endIndex)
                : new ArrayList<>();
        
        return PaginatedResponse.<Contact>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .isFirst(page == 1)
                .isLast(page >= totalPages)
                .build();
    }

    @CacheEvict(value = CONTACTS_CACHE, key = "'all'")
    public void evictContactsCache() {
        log.info("Evicting contacts cache");
    }
}
