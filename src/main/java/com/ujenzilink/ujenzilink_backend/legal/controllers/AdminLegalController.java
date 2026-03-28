package com.ujenzilink.ujenzilink_backend.legal.controllers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ujenzilink.ujenzilink_backend.auth.admin.enums.AdminActionType;
import com.ujenzilink.ujenzilink_backend.auth.admin.services.AdminAuditService;
import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.legal.dtos.LegalDocumentDto;
import com.ujenzilink.ujenzilink_backend.legal.dtos.UpdateLegalDocRequest;
import com.ujenzilink.ujenzilink_backend.legal.services.LegalAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/legal")
@CrossOrigin
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminLegalController {

    private final LegalAdminService legalAdminService;
    private final ObjectMapper customMapper;
    private final AdminAuditService adminAuditService;
    private final HttpServletRequest httpServletRequest;

    public AdminLegalController(LegalAdminService legalAdminService,
                                AdminAuditService adminAuditService,
                                HttpServletRequest httpServletRequest) {
        this.legalAdminService = legalAdminService;
        this.adminAuditService = adminAuditService;
        this.httpServletRequest = httpServletRequest;
        this.customMapper = new ObjectMapper();
        this.customMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    private String extractContent(String rawJson) {
        try {
            Map<String, String> map = customMapper.readValue(rawJson, new TypeReference<Map<String, String>>() {});
            String content = map.get("content");
            if (content == null || content.isBlank()) {
                throw new IllegalArgumentException("Content cannot be blank");
            }
            return content;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed JSON request: " + e.getMessage());
        }
    }

    @PostMapping("/terms")
    public ResponseEntity<ApiCustomResponse<LegalDocumentDto>> updateTermsAndConditions(
            @RequestBody String rawJson) {
        String content = extractContent(rawJson);
        LegalDocumentDto updated = legalAdminService.updateTermsAndConditions(content);
        
        adminAuditService.logAction(
           AdminActionType.UPDATE_TERMS_AND_CONDITIONS,
            "TERMS-" + updated.version(),
            "Updated Terms and Conditions to version " + updated.version(),
            httpServletRequest
        );

        return ResponseEntity.ok(new ApiCustomResponse<>(
                updated,
                "Terms and Conditions updated successfully to version " + updated.version(),
                200
        ));
    }

    @PostMapping("/privacy")
    public ResponseEntity<ApiCustomResponse<LegalDocumentDto>> updatePrivacyPolicy(
            @RequestBody String rawJson) {
        String content = extractContent(rawJson);
        LegalDocumentDto updated = legalAdminService.updatePrivacyPolicy(content);
        
        adminAuditService.logAction(
            AdminActionType.UPDATE_PRIVACY_POLICY,
            "PRIVACY-" + updated.version(),
            "Updated Privacy Policy to version " + updated.version(),
            httpServletRequest
        );

        return ResponseEntity.ok(new ApiCustomResponse<>(
                updated,
                "Privacy Policy updated successfully to version " + updated.version(),
                200
        ));
    }
}
