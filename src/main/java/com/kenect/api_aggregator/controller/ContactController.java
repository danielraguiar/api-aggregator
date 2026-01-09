package com.kenect.api_aggregator.controller;

import com.kenect.api_aggregator.dto.PaginatedResponse;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.service.ContactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping(value = "/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<Contact>> getAllContacts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Received request to fetch contacts - page: {}, size: {}", page, size);
        
        PaginatedResponse<Contact> response = contactService.getContactsPaginated(page, size);
        
        log.info("Returning page {} with {} contacts out of {} total", 
                page, response.getContent().size(), response.getTotalElements());
        
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(response.getTotalPages()))
                .header("X-Current-Page", String.valueOf(response.getPage()))
                .body(response);
    }
}
