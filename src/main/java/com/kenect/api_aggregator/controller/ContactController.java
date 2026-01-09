package com.kenect.api_aggregator.controller;

import com.kenect.api_aggregator.dto.ContactQueryParams;
import com.kenect.api_aggregator.dto.PaginatedResponse;
import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.service.ContactService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@Validated
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping(value = "/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllContacts(@Valid ContactQueryParams params) {
        
        if (params.hasPaginationParams()) {
            log.info("Received request to fetch contacts - page: {}, size: {}, source: {}", 
                    params.getPageOrDefault(), params.getSizeOrDefault(), params.getSource());
            
            PaginatedResponse<Contact> response = contactService.getContactsPaginated(params);
            
            log.info("Returning page {} with {} contacts out of {} total", 
                    response.getPage(), response.getContent().size(), response.getTotalElements());
            
            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(response.getTotalElements()))
                    .header("X-Total-Pages", String.valueOf(response.getTotalPages()))
                    .header("X-Current-Page", String.valueOf(response.getPage()))
                    .body(response);
        } else {
            log.info("Received request to fetch all contacts - source: {}", params.getSource());
            
            List<Contact> contacts = contactService.getAllContacts(params.getSource());
            
            log.info("Returning {} contacts", contacts.size());
            
            return ResponseEntity.ok(contacts);
        }
    }
}
