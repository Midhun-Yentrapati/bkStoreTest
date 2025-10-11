package com.bookstore.user_authentication_service.repository;

import com.bookstore.user_authentication_service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.id = :userId AND prt.used = false ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findLatestUnusedTokenByUserId(@Param("userId") String userId);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.user.id = :userId AND prt.used = false")
    void markAllUserTokensAsUsed(@Param("userId") String userId);
    
    void deleteByUserId(String userId);
}