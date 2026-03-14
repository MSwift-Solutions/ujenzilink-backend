package com.ujenzilink.ujenzilink_backend.legal.repositories;

import com.ujenzilink.ujenzilink_backend.legal.models.PrivacyPolicyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivacyPolicyVersionRepository extends JpaRepository<PrivacyPolicyVersion, UUID> {

    @Query("SELECT v FROM PrivacyPolicyVersion v WHERE v.privacyPolicy.id = :privacyPolicyId ORDER BY v.revisionNumber DESC")
    List<PrivacyPolicyVersion> findByPrivacyPolicyIdOrderByRevisionNumberDesc(@Param("privacyPolicyId") UUID privacyPolicyId);

    @Query("SELECT v FROM PrivacyPolicyVersion v WHERE v.privacyPolicy.id = :privacyPolicyId AND v.version = :version")
    Optional<PrivacyPolicyVersion> findByPrivacyPolicyIdAndVersion(@Param("privacyPolicyId") UUID privacyPolicyId, @Param("version") String version);

    @Query("SELECT v FROM PrivacyPolicyVersion v " +
           "WHERE v.privacyPolicy.id = :privacyPolicyId " +
           "ORDER BY v.revisionNumber DESC LIMIT 1")
    Optional<PrivacyPolicyVersion> findLatestByPrivacyPolicyId(
            @Param("privacyPolicyId") UUID privacyPolicyId);

    @Query("SELECT COUNT(v) FROM PrivacyPolicyVersion v WHERE v.privacyPolicy.id = :privacyPolicyId")
    long countByPrivacyPolicyId(@Param("privacyPolicyId") UUID privacyPolicyId);
}
