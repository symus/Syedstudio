package com.craftora.subscription;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {

    List<SubscriptionPlan> findByActiveTrueOrderByPositionAsc();
}
