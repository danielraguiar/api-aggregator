package com.kenect.api_aggregator.dto;

import com.kenect.api_aggregator.model.ContactSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactQueryParams {

    @Min(value = 1, message = "Page number must be greater than 0")
    private Integer page;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private Integer size;

    private ContactSource source;

    public boolean hasPaginationParams() {
        return page != null || size != null;
    }

    public int getPageOrDefault() {
        return page != null ? page : 1;
    }

    public int getSizeOrDefault() {
        return size != null ? size : 20;
    }
}
