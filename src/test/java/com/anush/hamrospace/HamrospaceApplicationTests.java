package com.anush.hamrospace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.anush.hamrospace.common.pagination.PaginationRequest;
import com.anush.hamrospace.common.pagination.PaginationUtil;

class HamrospaceApplicationTests {

    @Test
    void invalidSortByFallsBackToCreatedAt() {
        PaginationRequest request = new PaginationRequest();
        request.setSortBy("passwordHash");
        request.setSortDirection("asc");

        Pageable pageable = PaginationUtil.of(request);

        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("passwordHash")).isNull();
    }

    @Test
    void whitelistedSortByIsPreserved() {
        PaginationRequest request = new PaginationRequest();
        request.setSortBy("rent");
        request.setSortDirection("desc");

        Pageable pageable = PaginationUtil.of(request);

        assertThat(pageable.getSort().getOrderFor("rent")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNull();
    }

    @Test
    void pageSizeIsClampedToMaximum() {
        PaginationRequest request = new PaginationRequest();
        request.setPageSize(500);

        Pageable pageable = PaginationUtil.of(request);

        assertThat(pageable.getPageSize()).isEqualTo(50);
    }
}
