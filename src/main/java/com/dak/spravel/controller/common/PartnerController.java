    package com.dak.spravel.controller.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.service.common.PartnerService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {
    private final PartnerService partnerService;

    @PostMapping
    @PreAuthorize("hasAuthority('partner.store')")
    public ResponseEntity<ResData<Partners>> createPartner(
            @Valid @RequestBody CreatePartnerRequest request,
            @AuthenticationPrincipal UserDetails userDetails, Authentication auth) {

        if (auth != null) {
            log.info("=== DEBUG SECURITY ===");
            log.info("User: {}", auth.getName());
            log.info("Authorities: {}", auth.getAuthorities());
            log.info("=======================");
        } else {
            System.out.println("=== AUTH NULL (Token Gak Valid / Gak Masuk Filter) ===");
        }

        log.info("[POST] /api/v1/partners - Request: {}", request);
        Partners createdPartner = partnerService.createPartner(request);
        return ResponseBuilder.ok(createdPartner);
    }
}