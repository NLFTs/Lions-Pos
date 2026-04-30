package com.dak.spravel.controller.common;

import org.springframework.http.ResponseEntity;
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
    
}