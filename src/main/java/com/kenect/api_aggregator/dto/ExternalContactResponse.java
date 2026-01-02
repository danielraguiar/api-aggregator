package com.kenect.api_aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalContactResponse {

    private List<ExternalContactDto> contacts;
    private PaginationMetadata pagination;
}
