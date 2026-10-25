package com.craftora.user;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    @Query("select t from UserToken t join fetch t.user where t.tokenHash = :tokenHash")
    Optional<UserToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from UserToken t where t.user.id = :userId and t.type = :type")
    void deleteAllForUser(UUID userId, UserToken.Type type);

    @Modifying
    @Query("delete from UserToken t where t.expiresAt < :now")
    int deleteExpired(Instant now);
}
