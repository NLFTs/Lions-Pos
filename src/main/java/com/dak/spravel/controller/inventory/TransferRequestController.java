package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.model.inventory.TransferRequest;
import com.dak.spravel.service.inventory.TransferRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    @GetMapping
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<List<TransferRequest>> index() {
        log.info("[GET] /api/v1/transfer-requests");
        return ResponseEntity.ok(transferRequestService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<Page<TransferRequest>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/transfer-requests/page page={} size={}", page, size);
        return ResponseEntity.ok(transferRequestService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.show')")
    public ResponseEntity<TransferRequest> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/transfer-requests/{}", id);
        return ResponseEntity.ok(transferRequestService.findById(id));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<List<TransferRequest>> getByPartner(@PathVariable Long partnerId) {
        log.info("[GET] /api/v1/transfer-requests/partner/{}", partnerId);
        return ResponseEntity.ok(transferRequestService.findByPartnerId(partnerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('transfer_request.store')")
    public ResponseEntity<TransferRequest> store(@Valid @RequestBody TransferRequestDTO request) {
        log.info("[POST] /api/v1/transfer-requests partnerId={}", request.getPartnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transferRequestService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('transfer_request.update')")
    public ResponseEntity<TransferRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("[PATCH] /api/v1/transfer-requests/{}/status status={}", id, status);
        return ResponseEntity.ok(transferRequestService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/transfer-requests/{}", id);
        transferRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}