package com.dak.spravel.controller.inventory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.inventoryresponse.TransferRequestResponse;
import com.dak.spravel.service.inventory.TransferRequestService;
import com.dak.spravel.util.ResponseBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    //Dipakai Superadmin untuk melihat semua data (Paginated)
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<Page<TransferRequestResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/transfer-requests/admin - Superadmin access, page: {}, size: {}", page, size);
        return ResponseBuilder.ok(transferRequestService.findAll(page, size));
    }

    //Mengambil data list milik partner yang sedang login (Non-paginated)
    @GetMapping
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<List<TransferRequestResponse>>> index() {
        log.info("[GET] /api/v1/transfer-requests");
        // Di Service, method findAll() sudah mengembalikan List<TransferRequest>. 
        // Kita map ke Response DTO agar seragam dengan standard respons data kamu.
        List<TransferRequestResponse> responses = transferRequestService.findAll().stream()
                .map(transferRequestService::mapToResponse)
                .toList();
        return ResponseBuilder.ok(responses);
    }

    //Mengambil data dengan paging (Otomatis memfilter data partner atau superadmin)
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<Page<TransferRequestResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/transfer-requests/page page={} size={}", page, size);
        return ResponseBuilder.ok(transferRequestService.findAll(page, size));
    }

    //Melihat detail 1 data transfer request berdasarkan ID dengan proteksi hak akses partner
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.show')")
    public ResponseEntity<ResData<TransferRequestResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/transfer-requests/{}", id);
        return ResponseBuilder.ok(transferRequestService.findById(id));
    }

    //Mengambil list data transfer khusus ID partner tertentu
    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<List<TransferRequestResponse>>> getByPartner(@PathVariable Long partnerId) {
        log.info("[GET] /api/v1/transfer-requests/partner/{}", partnerId);
        // Kita konversi output List<TransferRequest> dari service menjadi List<TransferRequestResponse>
        // dan dibungkus menggunakan ResponseBuilder.ok agar format JSON-nya rapi & seragam
        List<TransferRequestResponse> responses = transferRequestService.findByPartnerId(partnerId).stream()
                .map(transferRequestService::mapToResponse)
                .toList();
        return ResponseBuilder.ok(responses);
    }

    //Membuat data request transfer baru dengan otomasi pengecekan Gudang & Cabang
    @PostMapping
    @PreAuthorize("hasAuthority('transfer_request.store')")
    public ResponseEntity<ResData<TransferRequestResponse>> store(@Valid @RequestBody TransferRequestDTO request) {
        log.info("[POST] /api/v1/transfer-requests partnerId={}", request.getPartnerId());
        return ResponseBuilder.created(transferRequestService.create(request));
    }

    //Proses soft-delete (Mengisi flag deleted_at & deleted_by ke DB)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/transfer-requests/{}", id);
        transferRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Update status: pending→approved, approved→received
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('transfer_request.update')")
    public ResponseEntity<ResData<TransferRequestResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestBody(required = false) java.util.List<com.dak.spravel.dto.request.inventory.TransferRequestItemDTO> items) {
        log.info("[PATCH] /api/v1/transfer-requests/{}/status status={}", id, status);
        TransferRequestResponse result;
        if ("received".equalsIgnoreCase(status)) {
            result = transferRequestService.receiveTransfer(id, items != null ? items : java.util.Collections.emptyList());
        } else {
            result = transferRequestService.updateStatus(id, status);
        }
        return ResponseBuilder.ok(result);
    }
}