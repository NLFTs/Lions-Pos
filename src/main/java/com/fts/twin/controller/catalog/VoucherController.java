package com.fts.twin.controller.catalog;

import com.fts.twin.dto.request.product.VoucherRequest;
import com.fts.twin.dto.response.catalogresponse.VoucherResponse;
import com.fts.twin.service.catalog.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasAuthority('voucher.index')")
    public ResponseEntity<List<VoucherResponse>> getAllVouchers() {
        log.info("[GET] /api/v1/vouchers");
        return ResponseEntity.ok(voucherService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.show')")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Long id) {
        log.info("[GET] /api/v1/vouchers/{}", id);
        return ResponseEntity.ok(voucherService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('voucher.store')")
    public ResponseEntity<VoucherResponse> createVoucher(
            @Valid @RequestBody VoucherRequest request) {
        log.info("[POST] /api/v1/vouchers - Request: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.update')")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request) {
        log.info("[PUT] /api/v1/vouchers/{}", id);
        return ResponseEntity.ok(voucherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('voucher.delete')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/vouchers/{}", id);
        voucherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}