package com.craftora.storefront;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorefrontTemplateRepository extends JpaRepository<StorefrontTemplate, String> {

    List<StorefrontTemplate> findByActiveTrueOrderByPositionAsc();
}
