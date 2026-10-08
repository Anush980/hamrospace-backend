package com.anush.hamrospace.user.repository;

import java.util.Optional;

import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.enums.UserRole;
import com.anush.hamrospace.user.enums.UserStatus;

public interface UserRepository extends AbstractRepository<User> {
    
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);
} 
