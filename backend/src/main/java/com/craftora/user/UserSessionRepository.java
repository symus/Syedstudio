package com.craftora.user;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    @Query("select s from UserSession s join fetch s.user where s.tokenHash = :tokenHash")
    Optional<UserSession> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from UserSession s where s.tokenHash = :tokenHash")
    void deleteByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from UserSession s where s.user.id = :userId")
    void deleteAllForUser(UUID userId);

    @Modifying
    @Query("delete from UserSession s where s.expiresAt < :now")
    int deleteExpired(Instant now);
}
