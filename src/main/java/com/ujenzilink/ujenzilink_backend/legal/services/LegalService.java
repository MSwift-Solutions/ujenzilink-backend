package com.ujenzilink.ujenzilink_backend.legal.services;

import com.ujenzilink.ujenzilink_backend.legal.dtos.LatestLegalDocsResponse;
import com.ujenzilink.ujenzilink_backend.legal.dtos.LegalDocumentDto;
import com.ujenzilink.ujenzilink_backend.legal.models.PrivacyPolicy;
import com.ujenzilink.ujenzilink_backend.legal.models.TermsAndConditions;
import com.ujenzilink.ujenzilink_backend.legal.repositories.PrivacyPolicyRepository;
import com.ujenzilink.ujenzilink_backend.legal.repositories.TermsAndConditionsRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LegalService {

    private final TermsAndConditionsRepository termsRepo;
    private final PrivacyPolicyRepository privacyRepo;

    public LegalService(TermsAndConditionsRepository termsRepo, PrivacyPolicyRepository privacyRepo) {
        this.termsRepo = termsRepo;
        this.privacyRepo = privacyRepo;
    }

    public LatestLegalDocsResponse getLatestLegalDocuments() {
        TermsAndConditions activeTerms = termsRepo.findByIsActiveTrue().orElse(null);
        PrivacyPolicy activePrivacy = privacyRepo.findByIsActiveTrue().orElse(null);

        LegalDocumentDto termsDto = null;
        if (activeTerms != null) {
            termsDto = new LegalDocumentDto(
                    activeTerms.getTitle(),
                    activeTerms.getVersion(),
                    activeTerms.getContent(),
                    activeTerms.getUpdatedAt() != null ? activeTerms.getUpdatedAt() : activeTerms.getCreatedAt()
            );
        } else {
            // Fallback for when DB has no terms created yet
            termsDto = new LegalDocumentDto(
                    "Terms and Conditions",
                    "1.0",
                    "# Terms and Conditions\n\nNo terms defined yet.",
                    Instant.now()
            );
        }

        LegalDocumentDto privacyDto = null;
        if (activePrivacy != null) {
            privacyDto = new LegalDocumentDto(
                    activePrivacy.getTitle(),
                    activePrivacy.getVersion(),
                    activePrivacy.getContent(),
                    activePrivacy.getUpdatedAt() != null ? activePrivacy.getUpdatedAt() : activePrivacy.getCreatedAt()
            );
        } else {
            // Fallback for when DB has no privacy policy created yet
            privacyDto = new LegalDocumentDto(
                    "Privacy Policy",
                    "1.0",
                    "# Privacy Policy\n\nNo privacy policy defined yet.",
                    Instant.now()
            );
        }

        return new LatestLegalDocsResponse(termsDto, privacyDto);
    }
}
