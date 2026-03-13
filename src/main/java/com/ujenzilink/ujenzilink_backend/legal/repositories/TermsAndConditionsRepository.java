package com.ujenzilink.ujenzilink_backend.legal.repositories;

import com.ujenzilink.ujenzilink_backend.legal.models.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, UUID> {

    Optional<TermsAndConditions> findByIsActiveTrue();

    Optional<TermsAndConditions> findByVersion(String version);

    List<TermsAndConditions> findAllByOrderByCreatedAtDesc();

    boolean existsByVersion(String version);

    @Modifying
    @Query("UPDATE TermsAndConditions t SET t.isActive = false WHERE t.isActive = true")
    void deactivateAll();

    @Query("SELECT COALESCE(MAX(v.revisionNumber), 0) " +
           "FROM TermsAndConditionsVersion v " +
           "WHERE v.termsAndConditions.id = :termsAndConditionsId")
    int findLatestRevisionNumber(@Param("termsAndConditionsId") UUID termsAndConditionsId);
}
