package com.dak.spravel.controller.common;

import com.dak.spravel.dto.request.partner.CreatePartnerRequest;
import com.dak.spravel.dto.request.partner.UpdatePartnerRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.common.PartnerResponse;
import com.dak.spravel.service.common.PartnerService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('partner.index')")
    public ResponseEntity<ResData<List<PartnerResponse>>> indexAdmin() {
        log.info("[GET] /partners/admin");
        return ResponseBuilder.ok(partnerService.findAll());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('partner.index')")
    public ResponseEntity<ResData<List<PartnerResponse>>> findAll() {
        log.info("[GET] /partners");
        return ResponseBuilder.ok(partnerService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('partner.index')")
    public ResponseEntity<ResData<Page<PartnerResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /partners/page page={} size={}", page, size);
        return ResponseBuilder.ok(partnerService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('partner.show')")
    public ResponseEntity<ResData<PartnerResponse>> findById(@PathVariable Long id) {
        log.info("[GET] /partners/{}", id);
        return ResponseBuilder.ok(partnerService.findById(id));
    }


    @PostMapping
    @PreAuthorize("hasAuthority('partner.store')")
    public ResponseEntity<ResData<PartnerResponse>> create(
            @Valid @RequestBody CreatePartnerRequest request) {
        log.info("[POST] /partners");
        return ResponseBuilder.ok(partnerService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('partner.update')")
    public ResponseEntity<ResData<PartnerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePartnerRequest request) {
        log.info("[PUT] /partners/{}", id);
        return ResponseBuilder.ok(partnerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('partner.delete')")
    public ResponseEntity<ResData<PartnerResponse>> softDelete(@PathVariable Long id) {
        log.info("[DELETE] /partners/{}", id);
        return ResponseBuilder.ok(partnerService.softDelete(id));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('partner.restore')")
    public ResponseEntity<ResData<PartnerResponse>> restore(@PathVariable Long id) {
        log.info("[POST] /partners/{}/restore", id);
        return ResponseBuilder.ok(partnerService.restore(id));
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasAuthority('partner.force_delete')")
    public ResponseEntity<ResData<Void>> hardDelete(@PathVariable Long id) {
        log.info("[DELETE] /partners/{}/force", id);
        partnerService.hardDelete(id);
        return ResponseBuilder.ok();
    }
}