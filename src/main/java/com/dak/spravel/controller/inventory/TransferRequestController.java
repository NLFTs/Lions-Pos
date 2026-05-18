package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.inventoryresponse.TransferRequestResponse;
import com.dak.spravel.model.inventory.TransferRequest;
import com.dak.spravel.service.inventory.TransferRequestService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<Page<TransferRequestResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/transfer-requests/admin - Superadmin access, page: {}, size: {}", page, size);
        return ResponseBuilder.ok(transferRequestService.findAll(page, size));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<List<TransferRequestResponse>>> index() {
        log.info("[GET] /api/v1/transfer-requests");
        return ResponseBuilder.ok(transferRequestService.findAll().stream()
                .map(transferRequestService::mapToResponse)
                .toList());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<ResData<Page<TransferRequestResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/transfer-requests/page page={} size={}", page, size);
        return ResponseBuilder.ok(transferRequestService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.show')")
    public ResponseEntity<ResData<TransferRequestResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/transfer-requests/{}", id);
        return ResponseBuilder.ok(transferRequestService.findById(id));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAuthority('transfer_request.index')")
    public ResponseEntity<List<TransferRequest>> getByPartner(@PathVariable Long partnerId) {
        log.info("[GET] /api/v1/transfer-requests/partner/{}", partnerId);
        return ResponseEntity.ok(transferRequestService.findByPartnerId(partnerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('transfer_request.store')")
    public ResponseEntity<ResData<TransferRequestResponse>> store(@Valid @RequestBody TransferRequestDTO request) {
        log.info("[POST] /api/v1/transfer-requests partnerId={}", request.getPartnerId());
        return ResponseBuilder.created(transferRequestService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('transfer_request.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/transfer-requests/{}", id);
        transferRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}