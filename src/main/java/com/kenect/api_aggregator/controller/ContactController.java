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

import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping(value = "/contacts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllContacts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        if (page != null || size != null) {
            int pageNumber = page != null ? page : 1;
            int pageSize = size != null ? size : 20;
            
            log.info("Received request to fetch contacts - page: {}, size: {}", pageNumber, pageSize);
            
            PaginatedResponse<Contact> response = contactService.getContactsPaginated(pageNumber, pageSize);
            
            log.info("Returning page {} with {} contacts out of {} total", 
                    pageNumber, response.getContent().size(), response.getTotalElements());
            
            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(response.getTotalElements()))
                    .header("X-Total-Pages", String.valueOf(response.getTotalPages()))
                    .header("X-Current-Page", String.valueOf(response.getPage()))
                    .body(response);
        } else {
            log.info("Received request to fetch all contacts");
            
            List<Contact> contacts = contactService.getAllContacts();
            
            log.info("Returning {} contacts", contacts.size());
            
            return ResponseEntity.ok(contacts);
        }
    }
}
