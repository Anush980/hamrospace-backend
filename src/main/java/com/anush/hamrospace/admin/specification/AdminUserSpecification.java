package com.anush.hamrospace.admin.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.anush.hamrospace.admin.dto.request.AdminUserFilterRequest;
import com.anush.hamrospace.user.entity.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class AdminUserSpecification {

    private AdminUserSpecification() {
    }

    public static Specification<User> buildSpecification(AdminUserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter != null) {
                if (filter.role() != null) {
                    predicates.add(cb.equal(root.get("role"), filter.role()));
                }
                if (filter.status() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.status()));
                }
                if (StringUtils.hasText(filter.keyword())) {
                    String pattern = "%" + filter.keyword().trim().toLowerCase(Locale.ROOT) + "%";
                    Join<Object, Object> profile = root.join("profile", JoinType.LEFT);
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("email")), pattern),
                            cb.like(cb.lower(cb.coalesce(profile.get("fullName"), "")), pattern)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
