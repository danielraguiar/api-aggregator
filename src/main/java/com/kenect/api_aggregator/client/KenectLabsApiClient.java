package com.kenect.api_aggregator.client;

import com.kenect.api_aggregator.dto.ExternalContactDto;
import com.kenect.api_aggregator.dto.ExternalContactResponse;
import com.kenect.api_aggregator.dto.PaginationMetadata;
import com.kenect.api_aggregator.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class KenectLabsApiClient {

    private static final String CONTACTS_ENDPOINT = "/api/v1/contacts";
    private static final String PAGE_PARAM = "page";
    private static final Pattern LINK_PATTERN = Pattern.compile("<([^>]+)>;\\s*rel=\"([^\"]+)\"");

    private final WebClient webClient;

    public KenectLabsApiClient(@Qualifier("kenectApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Retryable(
            retryFor = {WebClientResponseException.class, ExternalApiException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    public ExternalContactResponse fetchContactsPage(int page) {
        log.debug("Fetching contacts page: {} (with retry support)", page);

        try {
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(CONTACTS_ENDPOINT)
                            .queryParam(PAGE_PARAM, page)
                            .build())
                    .exchangeToMono(clientResponse -> {
                        HttpHeaders headers = clientResponse.headers().asHttpHeaders();
                        PaginationMetadata pagination = parsePaginationHeaders(headers);

                        return clientResponse.bodyToMono(new ParameterizedTypeReference<List<ExternalContactDto>>() {})
                                .map(contacts -> ExternalContactResponse.builder()
                                        .contacts(contacts)
                                        .pagination(pagination)
                                        .build());
                    })
                    .block();

            log.debug("Successfully fetched page {}: {} contacts", page,
                    response != null && response.getContacts() != null ? response.getContacts().size() : 0);

            return response;

        } catch (WebClientResponseException ex) {
            log.error("API request failed with status {}: {}", ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException(
                    "Failed to fetch contacts from external API. Status: " + ex.getStatusCode(),
                    ex
            );
        } catch (Exception ex) {
            log.error("Unexpected error while fetching contacts: {}", ex.getMessage(), ex);
            throw new ExternalApiException("Unexpected error while communicating with external API", ex);
        }
    }

    @Recover
    public ExternalContactResponse recoverFromApiFailure(Exception ex, int page) {
        log.error("All retry attempts exhausted for page {}. Failing gracefully.", page, ex);
        throw new ExternalApiException(
                "Failed to fetch contacts after multiple retry attempts for page " + page,
                ex
        );
    }

    private PaginationMetadata parsePaginationHeaders(HttpHeaders headers) {
        PaginationMetadata.PaginationMetadataBuilder builder = PaginationMetadata.builder();

        String linkHeader = headers.getFirst("Link");
        if (linkHeader != null) {
            parseLinkHeader(linkHeader, builder);
        }

        String currentPage = headers.getFirst("Current-Page");
        if (currentPage != null) {
            builder.currentPage(Integer.parseInt(currentPage));
        }

        String pageItems = headers.getFirst("Page-Items");
        if (pageItems != null) {
            builder.pageItems(Integer.parseInt(pageItems));
        }

        String totalPages = headers.getFirst("Total-Pages");
        if (totalPages != null) {
            builder.totalPages(Integer.parseInt(totalPages));
        }

        String totalCount = headers.getFirst("Total-Count");
        if (totalCount != null) {
            builder.totalCount(Integer.parseInt(totalCount));
        }

        return builder.build();
    }

    private void parseLinkHeader(String linkHeader, PaginationMetadata.PaginationMetadataBuilder builder) {
        Matcher matcher = LINK_PATTERN.matcher(linkHeader);

        while (matcher.find()) {
            String url = matcher.group(1);
            String rel = matcher.group(2);

            switch (rel) {
                case "next" -> builder.nextPageUrl(url);
                case "prev" -> builder.prevPageUrl(url);
                case "first" -> builder.firstPageUrl(url);
                case "last" -> builder.lastPageUrl(url);
            }
        }
    }
}
