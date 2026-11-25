package com.anush.hamrospace.booking.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.anush.hamrospace.booking.dto.request.BookingFilterRequest;
import com.anush.hamrospace.booking.entity.Booking;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class BookingSpecification {

    public static Specification<Booking> buildSpecification(UUID guestId, BookingFilterRequest filter) {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("guest").get("id"), guestId));

            if (filter != null) {
                if (filter.bookingFeeStatus() != null) {
                    predicates.add(cb.equal(root.get("bookingFeeStatus"), filter.bookingFeeStatus()));
                }
                if (filter.bookingStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.bookingStatus()));
                }
                if (filter.paymentMethod() != null) {
                    predicates.add(cb.equal(root.get("paymentMethod"), filter.paymentMethod()));
                }
                if (StringUtils.hasText(filter.keyword())) {
                    String searchPattern = "%" + filter.keyword().trim().toLowerCase() + "%";
                    Join<Object, Object> listing = root.join("listing", JoinType.LEFT);
                    Join<Object, Object> guest = root.join("guest", JoinType.LEFT);
                    Join<Object, Object> profile = guest.join("profile", JoinType.LEFT);

                    predicates.add(cb.or(
                            cb.like(cb.lower(listing.get("title")), searchPattern),
                            cb.like(cb.lower(listing.get("address")), searchPattern),
                            cb.like(cb.lower(guest.get("email")), searchPattern),
                            cb.like(cb.lower(profile.get("fullName")), searchPattern)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
