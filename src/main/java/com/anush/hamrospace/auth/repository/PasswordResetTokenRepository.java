package com.anush.hamrospace.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.anush.hamrospace.auth.entity.PasswordResetToken;
import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.user.entity.User;

public interface PasswordResetTokenRepository extends AbstractRepository<PasswordResetToken> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :now WHERE t.user = :user AND t.usedAt IS NULL")
    void invalidateAllByUser(User user, Instant now);
}