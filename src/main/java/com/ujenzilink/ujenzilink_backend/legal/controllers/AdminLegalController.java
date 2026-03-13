package com.ujenzilink.ujenzilink_backend.legal.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.legal.dtos.LegalDocumentDto;
import com.ujenzilink.ujenzilink_backend.legal.dtos.UpdateLegalDocRequest;
import com.ujenzilink.ujenzilink_backend.legal.services.LegalAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/legal")
@CrossOrigin
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminLegalController {

    private final LegalAdminService legalAdminService;

    public AdminLegalController(LegalAdminService legalAdminService) {
        this.legalAdminService = legalAdminService;
    }

    @PostMapping("/terms")
    public ResponseEntity<ApiCustomResponse<LegalDocumentDto>> updateTermsAndConditions(
            @RequestBody @Valid UpdateLegalDocRequest request) {
        LegalDocumentDto updated = legalAdminService.updateTermsAndConditions(request.content());
        return ResponseEntity.ok(new ApiCustomResponse<>(
                updated,
                "Terms and Conditions updated successfully to version " + updated.version(),
                200
        ));
    }

    @PostMapping("/privacy")
    public ResponseEntity<ApiCustomResponse<LegalDocumentDto>> updatePrivacyPolicy(
            @RequestBody @Valid UpdateLegalDocRequest request) {
        LegalDocumentDto updated = legalAdminService.updatePrivacyPolicy(request.content());
        return ResponseEntity.ok(new ApiCustomResponse<>(
                updated,
                "Privacy Policy updated successfully to version " + updated.version(),
                200
        ));
    }
}
