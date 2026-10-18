package com.anush.hamrospace.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.anush.hamrospace.auth.entity.EmailVerificationToken;
import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.user.entity.User;

public interface EmailVerificationTokenRepository extends AbstractRepository<EmailVerificationToken> {

    Optional<EmailVerificationToken> findByToken(String token);

    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.usedAt = :now WHERE t.user = :user AND t.usedAt IS NULL")
    void invalidateAllByUser(User user, Instant now);
    
}