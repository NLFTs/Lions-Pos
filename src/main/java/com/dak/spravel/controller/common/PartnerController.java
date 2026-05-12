package com.dak.spravel.controller.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
import com.dak.spravel.dto.request.partner.UpdatePartnerRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.common.PartnerResponse;
import com.dak.spravel.service.common.PartnerService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    @PreAuthorize("hasAuthority('partner.index')")
    public ResponseEntity<ResData<List<PartnerResponse>>> findAll() {
        log.info("[GET] /api/v1/partners");
        return ResponseBuilder.ok(partnerService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('partner.show')")
    public ResponseEntity<ResData<PartnerResponse>> findById(@PathVariable Long id) {
        log.info("[GET] /api/v1/partners/{}", id);
        return ResponseBuilder.ok(partnerService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('partner.store')")
    public ResponseEntity<ResData<PartnerResponse>> createPartner(
            @Valid @RequestBody CreatePartnerRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            Authentication auth) {
        // if (auth != null) {
        //     log.info("=== DEBUG SECURITY ===");
        //     log.info("User: {}", auth.getName());
        //     log.info("Authorities: {}", auth.getAuthorities());
        //     log.info("=======================");
        // }
        log.info("[POST] /api/v1/partners - Request: {}", request);
        return ResponseBuilder.ok(partnerService.createPartner(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('partner.update')")
    public ResponseEntity<ResData<PartnerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartnerRequest request) {
        log.info("[PUT] /api/v1/partners/{}", id);
        return ResponseBuilder.ok(partnerService.update(id, request));
    }

    @PutMapping("soft-delete/{id}")
    @PreAuthorize("hasAuthority('partner.update')")
    public ResponseEntity<ResData<PartnerResponse>> softDelete(@PathVariable Long id) {
        log.info("[SOFT-DELETE] /api/v1/partners/{}", id);
        partnerService.softDelete(id);
        return ResponseBuilder.ok(partnerService.softDelete(id));
    }

    @PutMapping("restore/{id}")
    @PreAuthorize("hasAuthority('partner.update')")
    public ResponseEntity<ResData<PartnerResponse>> restore(@PathVariable Long id) {
        log.info("[RESTORE] /api/v1/partners/{}", id);
        partnerService.restore(id);
        return ResponseBuilder.ok(partnerService.restore(id));
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasAuthority('partner.delete')")
    public ResponseEntity<ResData<Void>> hardDelete(@PathVariable Long id) {
        log.info("[DELETE FORCE] /api/v1/partners/{}", id);
        partnerService.hardDelete(id);
        return ResponseBuilder.ok();
    }
}