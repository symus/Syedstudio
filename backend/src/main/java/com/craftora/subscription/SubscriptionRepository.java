package com.craftora.subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByMakerId(UUID makerId);

    @Query("select s.plan.code, count(s) from Subscription s group by s.plan.code")
    List<Object[]> countByPlan();
}
