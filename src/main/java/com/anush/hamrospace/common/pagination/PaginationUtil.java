package com.anush.hamrospace.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PaginationUtil {

    public Pageable of(PaginationRequest request) {

        int page = page(request.getPageNumber());
        int size = size(request.getPageSize());

        Sort sort = sort(request.getSortBy(), request.getSortDirection());

        return PageRequest.of(page, size, sort);
    }

    public Pageable of(int page, int size) {
        return PageRequest.of(page, size(page));
    }

    public Pageable of(int page, int size, String sortBy, String direction) {
        return PageRequest.of(page, size(size), sort(sortBy, direction));
    }

    // helpers

    private int page(Integer pageNumber) {
        return (pageNumber == null || pageNumber < 0)
                ? PaginationConfig.DEFAULT_PAGE_NUMBER
                : pageNumber;
    }

    private int size(Integer pageSize) {
        if (pageSize == null || pageSize < 1)
            return PaginationConfig.DEFAULT_PAGE_SIZE;

        return Math.min(pageSize, PaginationConfig.MAX_PAGE_SIZE);
    }

    private Sort sort(String sortBy, String direction) {

        String field = (sortBy == null || sortBy.isBlank())
                ? PaginationConfig.DEFAULT_SORT_FIELD
                : sortBy;

        boolean isDesc = "desc".equalsIgnoreCase(direction);

        return isDesc
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }
}