package com.jiraclone.repository;

import com.jiraclone.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Query(value = "SELECT * FROM refresh_tokens WHERE token = :token", nativeQuery = true)
    RefreshToken findByToken(String token);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId", nativeQuery = true)
    void revokeAllByUserId(UUID userId);

    @Modifying
    @Query(value = "DELETE FROM refresh_tokens WHERE expires_at < :now OR revoked = true", nativeQuery = true)
    int deleteExpiredOrRevoked(Instant now);
}