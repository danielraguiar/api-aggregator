package com.kenect.api_aggregator.controller;

import com.kenect.api_aggregator.model.Contact;
import com.kenect.api_aggregator.service.ContactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<List<Contact>> getAllContacts() {
        log.info("Received request to fetch all contacts");
        List<Contact> contacts = contactService.getAllContacts();
        log.info("Returning {} contacts", contacts.size());
        return ResponseEntity.ok(contacts);
    }
}
