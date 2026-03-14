package com.ujenzilink.ujenzilink_backend.legal.repositories;

import com.ujenzilink.ujenzilink_backend.legal.models.TermsAndConditionsVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermsAndConditionsVersionRepository extends JpaRepository<TermsAndConditionsVersion, UUID> {
    @Query("SELECT v FROM TermsAndConditionsVersion v WHERE v.termsAndConditions.id = :termsAndConditionsId ORDER BY v.revisionNumber DESC")
    List<TermsAndConditionsVersion> findByTermsAndConditionsIdOrderByRevisionNumberDesc(@Param("termsAndConditionsId") UUID termsAndConditionsId);

    @Query("SELECT v FROM TermsAndConditionsVersion v WHERE v.termsAndConditions.id = :termsAndConditionsId AND v.version = :version")
    Optional<TermsAndConditionsVersion> findByTermsAndConditionsIdAndVersion(@Param("termsAndConditionsId") UUID termsAndConditionsId, @Param("version") String version);

    @Query("SELECT v FROM TermsAndConditionsVersion v " +
           "WHERE v.termsAndConditions.id = :termsAndConditionsId " +
           "ORDER BY v.revisionNumber DESC LIMIT 1")
    Optional<TermsAndConditionsVersion> findLatestByTermsAndConditionsId(
            @Param("termsAndConditionsId") UUID termsAndConditionsId);

    @Query("SELECT COUNT(v) FROM TermsAndConditionsVersion v WHERE v.termsAndConditions.id = :termsAndConditionsId")
    long countByTermsAndConditionsId(@Param("termsAndConditionsId") UUID termsAndConditionsId);
}
