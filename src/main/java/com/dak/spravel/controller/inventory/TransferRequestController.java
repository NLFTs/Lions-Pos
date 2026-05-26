package com.dak.spravel.controller.inventory;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.request.inventory.TransferRequestItemDTO;
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

    @GetMapping
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<List<TransferRequestResponse>>> index() {
        log.info("[GET] /api/v1/transfer-requests - Fetching list");
        return ResponseBuilder.ok(transferRequestService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.show')")
    public ResponseEntity<ResData<TransferRequestResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/transfer-requests/{}", id);
        return ResponseBuilder.ok(transferRequestService.findById(id));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<List<TransferRequestResponse>>> findAdmin() {
        log.info("[GET] /api/v1/transfer-requests/admin - Fetching admin list");
        return ResponseBuilder.ok(transferRequestService.findAllAdmin());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('transfer_request.store')")
    public ResponseEntity<ResData<TransferRequestResponse>> store(@Valid @RequestBody TransferRequestDTO request) {
        log.info("[POST] /api/v1/transfer-requests");
        return ResponseBuilder.created(transferRequestService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('transfer_request.update')")
    public ResponseEntity<ResData<TransferRequestResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("[PATCH] /api/v1/transfer-requests/{}/status status={}", id, status);
        TransferRequestResponse result = transferRequestService.updateStatus(id, status);
        return ResponseBuilder.ok(result);
    }

    @PatchMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('transfer_request.update')")
    public ResponseEntity<ResData<TransferRequestResponse>> receiveTransfer(
            @PathVariable Long id,
            @RequestBody List<TransferRequestItemDTO> items) {
        
        log.info("[PATCH] /api/v1/transfer-requests/{}/receive - Processing item acceptance", id);
        TransferRequestResponse result = transferRequestService.receiveTransfer(id, items != null ? items : java.util.Collections.emptyList());
        return ResponseBuilder.ok(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/transfer-requests/{}", id);
        transferRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}