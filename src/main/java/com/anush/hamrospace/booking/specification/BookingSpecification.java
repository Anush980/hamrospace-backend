package com.anush.hamrospace.booking.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.anush.hamrospace.booking.dto.request.BookingFilterRequest;
import com.anush.hamrospace.booking.entity.Booking;

import jakarta.persistence.criteria.Predicate;

public class BookingSpecification {

    public static Specification<Booking> buildSpecification(BookingFilterRequest filterRequest) {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if(Objects.nonNull(filterRequest)){
                if(StringUtils.hasText(filterRequest.keyword())){
                    String searchPattern = "%"+ filterRequest.keyword()+"%";
                }

                if(Objects.nonNull(filterRequest.bookingFeeStatus())){
                    predicates.add(cb.equal(root.get("bookingFeeStatus"),filterRequest.bookingFeeStatus()));
                }

            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
