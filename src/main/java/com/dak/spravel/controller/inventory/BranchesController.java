package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.partner.BranchRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.inventoryresponse.BranchResponse;
import com.dak.spravel.service.inventory.BranchesService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchesController { 

    private final BranchesService branchesService;

    @GetMapping
    @PreAuthorize("hasAuthority('branch.index')")
    public ResponseEntity<ResData<List<BranchResponse>>> index() {
        log.info("[GET] /api/v1/branches");
        return ResponseBuilder.ok(branchesService.findAll());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<BranchResponse>> getAllForAdmin() {
        log.info("[GET] /api/v1/branches/admin - Superadmin access");
        return ResponseEntity.ok(branchesService.findAllBranches());
    }

    @GetMapping("/admin/page")
    public ResponseEntity<Page<BranchResponse>> findPageAdmin( 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/branches/admin/page - page: {}, size: {}", page, size);
        return ResponseEntity.ok(branchesService.findPageAdmin(page, size));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('branch.index') or hasRole('owner')")
    public ResponseEntity<ResData<Page<BranchResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/branches/page page={} size={}", page, size);
        return ResponseBuilder.ok(branchesService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('branch.show')")
    public ResponseEntity<ResData<BranchResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/branches/{}", id);
        return ResponseBuilder.ok(branchesService.findById(id));
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('branch.show')")
    public ResponseEntity<ResData<List<com.dak.spravel.dto.response.UserResponse>>> getUsersByBranch(@PathVariable Long id) {
        log.info("[GET] /api/v1/branches/{}/users", id);
        return ResponseBuilder.ok(branchesService.getUsersByBranch(id));
    }

    @PutMapping("/{id}/manager")
    @PreAuthorize("hasAuthority('branch.update')")
    public ResponseEntity<ResData<BranchResponse>> transferManager(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Long> body) {
        Long newManagerId = body.get("userId");
        log.info("[PUT] /api/v1/branches/{}/manager userId={}", id, newManagerId);
        return ResponseBuilder.ok(branchesService.transferManager(id, newManagerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('branch.store')")
    public ResponseEntity<ResData<BranchResponse>> store(@Valid @RequestBody BranchRequest request) {
        log.info("[POST] /api/v1/branches name={}", request.getName());
        return ResponseBuilder.created(branchesService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('branch.update')")
    public ResponseEntity<ResData<BranchResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchRequest request) {
        log.info("[PUT] /api/v1/branches/{}", id);
        return ResponseBuilder.ok(branchesService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('branch.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/branches/{}", id);
        branchesService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/soft-delete/{id}")
    @PreAuthorize("hasAuthority('branch.update')")
    public ResponseEntity<BranchResponse> softDelete(@Valid @PathVariable Long id) {
        log.info("[PUT] /api/v1/branches/{}", id);
        return ResponseEntity.ok(branchesService.softDelete(id));
    }

    @PutMapping("/restore/{id}")
    @PreAuthorize("hasAuthority('branch.update')")
    public ResponseEntity<BranchResponse> restore(@Valid @PathVariable Long id) {
        log.info("[PUT] /api/v1/branches/{}", id);
        return ResponseEntity.ok(branchesService.restoreBranch(id));
    }
}