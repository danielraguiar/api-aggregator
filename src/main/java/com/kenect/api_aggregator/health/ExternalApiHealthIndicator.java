package com.kenect.api_aggregator.health;

import com.kenect.api_aggregator.client.KenectLabsApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final KenectLabsApiClient apiClient;

    public ExternalApiHealthIndicator(KenectLabsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Health health() {
        try {
            apiClient.fetchContactsPage(1);
            return Health.up()
                    .withDetail("externalApi", "Kenect Labs API")
                    .withDetail("status", "reachable")
                    .build();
        } catch (Exception e) {
            log.warn("External API health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("externalApi", "Kenect Labs API")
                    .withDetail("status", "unreachable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
