package com.dak.spravel.controller.catalog;

import com.dak.spravel.dto.request.product.VoucherRequest;
import com.dak.spravel.model.catalog.Voucher;
import com.dak.spravel.service.catalog.VoucherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public Voucher createVoucher(@Valid @RequestBody VoucherRequest request) {
        log.info("[POST] /api/v1/vouchers - Request: {}", request);
        return voucherService.create(request);
    }

    @GetMapping
    public List<Voucher> getAllVouchers() {
        log.info("[GET] /api/v1/vouchers");
        return voucherService.getAll();
    }

    @GetMapping("/{id}")
    public Voucher getVoucherById(@PathVariable Long id) {
        log.info("[GET] /api/v1/vouchers/{}", id);
        return voucherService.getById(id);
    }

    @PutMapping("/{id}")
    public Voucher updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request) {
        log.info("[PUT] /api/v1/vouchers/{}", id);
        return voucherService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteVoucher(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/vouchers/{}", id);
        voucherService.delete(id);
        return "Voucher deleted successfully";
    }
}