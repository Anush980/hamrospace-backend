package com.anush.hamrospace.admin.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.anush.hamrospace.admin.dto.request.AdminReviewFilterRequest;
import com.anush.hamrospace.review.entity.Review;

import jakarta.persistence.criteria.Predicate;

public final class AdminReviewSpecification {

    private AdminReviewSpecification() {
    }

    public static Specification<Review> buildSpecification(AdminReviewFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter != null) {
                if (filter.listingId() != null) {
                    predicates.add(cb.equal(root.get("listing").get("id"), filter.listingId()));
                }
                if (filter.hidden() != null) {
                    predicates.add(cb.equal(root.get("hidden"), filter.hidden()));
                }
                if (StringUtils.hasText(filter.keyword())) {
                    String pattern = "%" + filter.keyword().trim().toLowerCase(Locale.ROOT) + "%";
                    predicates.add(cb.like(cb.lower(root.get("comment")), pattern));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
