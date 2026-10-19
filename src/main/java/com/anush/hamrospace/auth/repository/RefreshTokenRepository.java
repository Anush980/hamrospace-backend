package com.anush.hamrospace.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anush.hamrospace.auth.entity.RefreshToken;
import com.anush.hamrospace.common.repository.AbstractRepository;
import com.anush.hamrospace.user.entity.User;

public interface RefreshTokenRepository extends AbstractRepository<RefreshToken> {
    
  
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.user = :user AND r.deviceInfo = :deviceInfo AND r.revoked = false")
    void revokeActiveTokensByUserAndDevice(
        @Param("user") User user, 
        @Param("deviceInfo") String deviceInfo, 
        @Param("now") Instant now
    );

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.user = :user AND r.revoked = false")
    void revokeAllActiveTokensByUser(@Param("user") User user, @Param("now") Instant now); 
}
