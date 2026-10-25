package com.craftora.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            select u from User u
            where :q is null or lower(u.email) like lower(concat('%', :q, '%'))
               or lower(u.displayName) like lower(concat('%', :q, '%'))
            """)
    Page<User> search(String q, Pageable pageable);
}
