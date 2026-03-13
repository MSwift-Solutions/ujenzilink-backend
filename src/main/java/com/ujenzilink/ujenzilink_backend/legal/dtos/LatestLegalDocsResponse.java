package com.ujenzilink.ujenzilink_backend.legal.dtos;

public record LatestLegalDocsResponse(
        LegalDocumentDto termsAndConditions,
        LegalDocumentDto privacyPolicy
) {}
