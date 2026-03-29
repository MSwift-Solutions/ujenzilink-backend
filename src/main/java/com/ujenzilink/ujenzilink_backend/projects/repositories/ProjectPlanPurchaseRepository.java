package com.ujenzilink.ujenzilink_backend.projects.repositories;

import com.ujenzilink.ujenzilink_backend.projects.models.ProjectPlanPurchase;
import com.ujenzilink.ujenzilink_backend.projects.enums.PlanPurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ProjectPlanPurchaseRepository extends JpaRepository<ProjectPlanPurchase, UUID> {

    boolean existsByPlanIdAndBuyerIdAndStatus(UUID planId, UUID buyerId, PlanPurchaseStatus status);
    
    Optional<ProjectPlanPurchase> findByPlanIdAndBuyerId(UUID planId, UUID buyerId);
}
