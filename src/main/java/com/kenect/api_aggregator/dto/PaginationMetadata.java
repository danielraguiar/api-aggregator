package com.kenect.api_aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationMetadata {

    private Integer currentPage;
    private Integer pageItems;
    private Integer totalPages;
    private Integer totalCount;
    private String nextPageUrl;
    private String prevPageUrl;
    private String firstPageUrl;
    private String lastPageUrl;

    public boolean hasNextPage() {
        return currentPage != null && totalPages != null && currentPage < totalPages;
    }
}
