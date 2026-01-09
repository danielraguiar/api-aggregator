package com.kenect.api_aggregator.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> healthStatus = new LinkedHashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", Instant.now().toString());
        healthStatus.put("application", "api-aggregator");
        return ResponseEntity.ok(healthStatus);
    }
}
