package com.anush.hamrospace.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

public interface UserRepository extends AbstractRepository<User> {
    
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailWithProfile(@Param("email") String email);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);
} 
