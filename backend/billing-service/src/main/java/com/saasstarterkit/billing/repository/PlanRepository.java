package com.saasstarterkit.billing.repository;

import com.saasstarterkit.billing.domain.model.Plan;
import com.saasstarterkit.billing.domain.model.PlanTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    
    Optional<Plan> findByExternalPlanId(String externalPlanId);
    
    List<Plan> findByTier(PlanTier tier);
    
    List<Plan> findByActive(boolean active);
    
    @Query("SELECT p FROM Plan p WHERE p.price <= :maxPrice AND p.active = true")
    List<Plan> findActivePlansByMaxPrice(@Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT p FROM Plan p WHERE p.maxUsers >= :minUsers AND p.active = true")
    List<Plan> findActivePlansByMinUsers(@Param("minUsers") Integer minUsers);
    
    @Query("SELECT p FROM Plan p WHERE p.maxStorageGB >= :minStorage AND p.active = true")
    List<Plan> findActivePlansByMinStorage(@Param("minStorage") Integer minStorage);
    
    List<Plan> findByProvider(String provider);
    
    boolean existsByExternalPlanId(String externalPlanId);
    
    boolean existsByNameAndTier(String name, PlanTier tier);
}
