package com.anush.hamrospace.listing.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.anush.hamrospace.listing.dto.request.ListingFilterRequest;
import com.anush.hamrospace.listing.entity.Listing;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;


public class ListingSpecification {

    public static Specification<Listing> buildSpecification(UUID hostId, ListingFilterRequest filter) {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (hostId != null) {
                predicates.add(cb.equal(root.get("host").get("id"), hostId));
            } else {
                // Public search must only expose listings that are switched on and bookable.
                // Status is intentionally not checked: newly created listings are PENDING by design.
                predicates.add(cb.isTrue(root.get("active")));
                predicates.add(cb.isTrue(root.get("available")));
            }

            if (filter != null) {
                if (filter.keyword() != null && !filter.keyword().isBlank()) {
                    String pattern = "%" + filter.keyword().trim().toLowerCase(Locale.ROOT) + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("title")), pattern),
                            cb.like(cb.lower(root.get("address")), pattern)));
                }
                if (filter.address() != null && !filter.address().isBlank()) {
                    String addressPattern = "%" + filter.address().trim().toLowerCase(Locale.ROOT) + "%";
                    predicates.add(cb.like(cb.lower(root.get("address")), addressPattern));
                }
                if (Boolean.TRUE.equals(filter.verifiedOnly())) {
                    predicates.add(cb.isTrue(root.get("verified")));
                }
                if (filter.minPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("rent"), filter.minPrice()));
                }
                if (filter.maxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("rent"), filter.maxPrice()));
                }
                if (filter.propertyType() != null) {
                    predicates.add(cb.equal(root.get("propertyType"), filter.propertyType()));
                }
                if (filter.amenityIds() != null && !filter.amenityIds().isEmpty()) {
                    // Match listings that have ALL selected amenities
                    for (UUID amenityId : filter.amenityIds()) {
                        Subquery<UUID> sub = query.subquery(UUID.class);
                        Root<Listing> subRoot = sub.from(Listing.class);
                        Join<Object, Object> amenities = subRoot.join("amenities");
                        sub.select(subRoot.get("id"))
                                .where(
                                        cb.equal(subRoot.get("id"), root.get("id")),
                                        cb.equal(amenities.get("id"), amenityId));
                        predicates.add(cb.exists(sub));
                    }
                }
                // TODO: radius search requires PostGIS ST_DWithin native query
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
