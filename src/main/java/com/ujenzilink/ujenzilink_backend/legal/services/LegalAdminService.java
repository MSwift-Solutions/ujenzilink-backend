package com.ujenzilink.ujenzilink_backend.legal.services;

import com.ujenzilink.ujenzilink_backend.legal.dtos.LegalDocumentDto;
import com.ujenzilink.ujenzilink_backend.legal.models.PrivacyPolicy;
import com.ujenzilink.ujenzilink_backend.legal.models.PrivacyPolicyVersion;
import com.ujenzilink.ujenzilink_backend.legal.models.TermsAndConditions;
import com.ujenzilink.ujenzilink_backend.legal.models.TermsAndConditionsVersion;
import com.ujenzilink.ujenzilink_backend.legal.repositories.PrivacyPolicyRepository;
import com.ujenzilink.ujenzilink_backend.legal.repositories.PrivacyPolicyVersionRepository;
import com.ujenzilink.ujenzilink_backend.legal.repositories.TermsAndConditionsRepository;
import com.ujenzilink.ujenzilink_backend.legal.repositories.TermsAndConditionsVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class LegalAdminService {

    private final TermsAndConditionsRepository termsRepo;
    private final TermsAndConditionsVersionRepository termsVersionRepo;
    private final PrivacyPolicyRepository privacyRepo;
    private final PrivacyPolicyVersionRepository privacyVersionRepo;

    public LegalAdminService(TermsAndConditionsRepository termsRepo,
                             TermsAndConditionsVersionRepository termsVersionRepo,
                             PrivacyPolicyRepository privacyRepo,
                             PrivacyPolicyVersionRepository privacyVersionRepo) {
        this.termsRepo = termsRepo;
        this.termsVersionRepo = termsVersionRepo;
        this.privacyRepo = privacyRepo;
        this.privacyVersionRepo = privacyVersionRepo;
    }

    @Transactional
    public LegalDocumentDto updateTermsAndConditions(String newContent) {
        TermsAndConditions activeTerms = termsRepo.findByIsActiveTrue().orElse(null);

        String oldVersion = "1.0.0";
        String oldContent = "";
        if (activeTerms != null) {
            oldVersion = activeTerms.getVersion();
            oldContent = activeTerms.getContent();
            
            if (oldContent.trim().equals(newContent.trim())) {
                throw new IllegalArgumentException("No changes were made to the document.");
            }
            
            termsRepo.deactivateAll(); // Disables existing terms
        }

        String nextVersion = calculateNextVersion(oldContent, newContent, oldVersion, activeTerms == null);

        // Save new active TermsAndConditions
        TermsAndConditions newTerms = new TermsAndConditions();
        newTerms.setTitle(activeTerms != null ? activeTerms.getTitle() : "Terms and Conditions");
        newTerms.setContent(newContent);
        newTerms.setVersion(nextVersion);
        newTerms.setIsActive(true);
        newTerms = termsRepo.save(newTerms);

        // Determine revision number for history
        int revision = termsRepo.findLatestRevisionNumber(newTerms.getId()) + 1;

        // Snapshot into History table
        TermsAndConditionsVersion newVersion = new TermsAndConditionsVersion();
        newVersion.setTermsAndConditions(newTerms);
        newVersion.setVersion(nextVersion);
        newVersion.setTitle(newTerms.getTitle());
        newVersion.setContent(newContent);
        newVersion.setRevisionNumber(revision);
        newVersion.setChangeSummary("Updated from version " + oldVersion + " to " + nextVersion);
        termsVersionRepo.save(newVersion);

        return new LegalDocumentDto(
                newTerms.getTitle(),
                newTerms.getVersion(),
                newTerms.getContent(),
                newTerms.getUpdatedAt() != null ? newTerms.getUpdatedAt() : newTerms.getCreatedAt()
        );
    }

    @Transactional
    public LegalDocumentDto updatePrivacyPolicy(String newContent) {
        PrivacyPolicy activePrivacy = privacyRepo.findByIsActiveTrue().orElse(null);

        String oldVersion = "1.0.0";
        String oldContent = "";
        if (activePrivacy != null) {
            oldVersion = activePrivacy.getVersion();
            oldContent = activePrivacy.getContent();
            
            if (oldContent.trim().equals(newContent.trim())) {
                throw new IllegalArgumentException("No changes were made to the document.");
            }
            
            privacyRepo.deactivateAll(); // Disables existing privacy policy
        }

        String nextVersion = calculateNextVersion(oldContent, newContent, oldVersion, activePrivacy == null);

        // Save new active PrivacyPolicy
        PrivacyPolicy newPrivacy = new PrivacyPolicy();
        newPrivacy.setTitle(activePrivacy != null ? activePrivacy.getTitle() : "Privacy Policy");
        newPrivacy.setContent(newContent);
        newPrivacy.setVersion(nextVersion);
        newPrivacy.setIsActive(true);
        newPrivacy = privacyRepo.save(newPrivacy);

        // Determine revision number for history
        int revision = privacyRepo.findLatestRevisionNumber(newPrivacy.getId()) + 1;

        // Snapshot into History table
        PrivacyPolicyVersion newVersion = new PrivacyPolicyVersion();
        newVersion.setPrivacyPolicy(newPrivacy);
        newVersion.setVersion(nextVersion);
        newVersion.setTitle(newPrivacy.getTitle());
        newVersion.setContent(newContent);
        newVersion.setRevisionNumber(revision);
        newVersion.setChangeSummary("Updated from version " + oldVersion + " to " + nextVersion);
        privacyVersionRepo.save(newVersion);

        return new LegalDocumentDto(
                newPrivacy.getTitle(),
                newPrivacy.getVersion(),
                newPrivacy.getContent(),
                newPrivacy.getUpdatedAt() != null ? newPrivacy.getUpdatedAt() : newPrivacy.getCreatedAt()
        );
    }

    private String calculateNextVersion(String oldText, String newText, String oldVersion, boolean isFirst) {

        if (isFirst) {
            return "1.0.0";
        }

        String[] parts = oldVersion.split("\\.");
        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 1;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        Set<String> words1 = new HashSet<>(Arrays.asList(oldText.toLowerCase().split("\\W+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(newText.toLowerCase().split("\\W+")));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        double similarityScore = union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();

        // Major change
        if (similarityScore < 0.7) {
            return (major + 1) + ".0.0";
        }

        // Minor change
        if (similarityScore < 0.9) {
            return major + "." + (minor + 1) + ".0";
        }

        // Small change -> patch increment
        return major + "." + minor + "." + (patch + 1);
    }
}
