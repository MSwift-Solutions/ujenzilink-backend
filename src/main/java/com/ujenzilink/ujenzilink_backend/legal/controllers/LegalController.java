package com.ujenzilink.ujenzilink_backend.legal.controllers;

import com.ujenzilink.ujenzilink_backend.configs.ApiCustomResponse;
import com.ujenzilink.ujenzilink_backend.legal.dtos.LatestLegalDocsResponse;
import com.ujenzilink.ujenzilink_backend.legal.services.LegalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/public/legal")
@CrossOrigin
public class LegalController {

    private final LegalService legalService;

    public LegalController(LegalService legalService) {
        this.legalService = legalService;
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiCustomResponse<LatestLegalDocsResponse>> getLatestLegalDocuments() {
        LatestLegalDocsResponse documents = legalService.getLatestLegalDocuments();
        
        return ResponseEntity.ok(new ApiCustomResponse<>(
                documents,
                "Successfully fetched the latest legal documents.",
                200
        ));
    }
}
