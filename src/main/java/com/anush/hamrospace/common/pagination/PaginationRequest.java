package com.anush.hamrospace.common.pagination;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PaginationRequest {

    @Min(value = 0, message = "Page number must be 0 or greater")
    private Integer pageNumber = PaginationConfig.DEFAULT_PAGE_NUMBER;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 50, message = "Page size must not exceed 50")
    private Integer pageSize = PaginationConfig.DEFAULT_PAGE_SIZE;

    private String sortBy = PaginationConfig.DEFAULT_SORT_FIELD;
    private String sortDirection = PaginationConfig.DEFAULT_SORT_DIRECTION;
}
