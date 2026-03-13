package com.ujenzilink.ujenzilink_backend.legal.repositories;

import com.ujenzilink.ujenzilink_backend.legal.models.PrivacyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivacyPolicyRepository extends JpaRepository<PrivacyPolicy, UUID> {

    Optional<PrivacyPolicy> findByIsActiveTrue();

    Optional<PrivacyPolicy> findByVersion(String version);

    List<PrivacyPolicy> findAllByOrderByCreatedAtDesc();

    boolean existsByVersion(String version);

    @Modifying
    @Query("UPDATE PrivacyPolicy p SET p.isActive = false WHERE p.isActive = true")
    void deactivateAll();

    @Query("SELECT COALESCE(MAX(v.revisionNumber), 0) " +
           "FROM PrivacyPolicyVersion v " +
           "WHERE v.privacyPolicy.id = :privacyPolicyId")
    int findLatestRevisionNumber(@Param("privacyPolicyId") UUID privacyPolicyId);
}
