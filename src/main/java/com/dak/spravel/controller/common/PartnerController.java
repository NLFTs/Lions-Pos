package com.dak.spravel.controller.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
import com.dak.spravel.dto.request.partner.GetPartnerByPlan;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.service.common.PartnerService;
import com.dak.spravel.util.ResponseBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {
    private final PartnerService partnerService;

    @PostMapping
    public ResponseEntity<ResData<Partners>> createPartner(
            @Valid @RequestBody CreatePartnerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("[POST] /api/v1/partners - Request: {}", request);
        Partners createdPartner = partnerService.createPartner(request);
        return ResponseBuilder.ok(createdPartner);
    }

    @GetMapping("/plan/{plan}")
    public ResponseEntity<ResData<Iterable<Partners>>> getPartnersByPlan(@PathVariable Partners.Plan plan) {
        log.info("[GET] /api/v1/partners/plan/{}", plan);
        Iterable<Partners> partners = partnerService.getPartnersByPlan(new GetPartnerByPlan(plan));
        return ResponseBuilder.ok(partners);
    }

    @GetMapping("/all")
    public ResponseEntity<ResData<Iterable<Partners>>> getAllPartners() {
        log.info("[GET] /api/v1/partners/all");
        Iterable<Partners> partners = partnerService.getAllPartners();
        return ResponseBuilder.ok(partners);
    }
    
    @PutMapping("/soft-delete/{id}")
    public ResponseEntity<ResData<Partners>> softDeletePartner(@Valid @PathVariable Long id) {
        log.info("[PUT] /api/v1/partners/soft-delete/{}", id);
        Partners deletedPartner = partnerService.softDeletePartner(id);
        return ResponseBuilder.ok(deletedPartner);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResData<Partners>> updatePartner(@Valid @PathVariable Long id,@RequestBody CreatePartnerRequest request) {
        log.info("[PUT] /api/v1/partners/{} - Request: {}", id, request);
        Partners updatedPartner = partnerService.updatePartnesr(id, request);
        return ResponseBuilder.ok(updatedPartner);
    }

}