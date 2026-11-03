package com.anush.hamrospace.amenity.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.anush.hamrospace.amenity.dto.request.AmenityFilterRequest;
import com.anush.hamrospace.amenity.entity.Amenity;

import jakarta.persistence.criteria.Predicate;

public class AmenitySpecification {

    public static Specification<Amenity> buildSpecification(AmenityFilterRequest request) {
        return (root, query, criteriaBuilder) -> {

            // Prevent DISTINCT mutation on Count Query & handle potential null query object
            if (query != null && Long.class != query.getResultType()) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (Objects.nonNull(request)) {

                if (StringUtils.hasText(request.search())) {
                    String searchPattern = "%" + request.search().trim().toUpperCase() + "%";

                    Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.upper(root.get("customerName")),
                            searchPattern);

                    predicates.add(criteriaBuilder.or(nameMatch));
                }

                if (Objects.nonNull(request.isActive())) {
                    predicates.add(criteriaBuilder.equal(root.get("isActive"), request.isActive()));
                }

                if (Objects.nonNull(request.isFeatured())) {
                    predicates.add(criteriaBuilder.equal(root.get("isFeatured"), request.isFeatured()));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }
}
