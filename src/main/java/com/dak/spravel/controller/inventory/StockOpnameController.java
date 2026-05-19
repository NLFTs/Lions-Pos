package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.StockOpnameCountRequest;
import com.dak.spravel.dto.request.inventory.StockOpnameRequest;
import com.dak.spravel.dto.response.inventoryresponse.StockOpnameItemResponse;
import com.dak.spravel.dto.response.inventoryresponse.StockOpnameResponse;
import com.dak.spravel.service.inventory.StockOpnameService;
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
@RequestMapping("/api/v1/stock-opnames")
@RequiredArgsConstructor
public class StockOpnameController {

    private final StockOpnameService stockOpnameService;

    // SUPER ADMIN ONLY
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<List<StockOpnameResponse>> getAllForAdmin() {
        log.info("[GET] /api/v1/stock-opnames/admin");
        return ResponseEntity.ok(stockOpnameService.findAllStockOpname());
    }

    // PARTNER / EMPLOYEE
    @GetMapping
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<List<StockOpnameResponse>> index() {
        log.info("[GET] /api/v1/stock-opnames");
        return ResponseEntity.ok(stockOpnameService.findAll());
    }

    // PAGINATION
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<Page<StockOpnameResponse>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-opnames/page page={} size={}", page, size);
        return ResponseEntity.ok(stockOpnameService.findAll(page, size));
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_opname.show')")
    public ResponseEntity<StockOpnameResponse> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-opnames/{}", id);
        return ResponseEntity.ok(stockOpnameService.findById(id));
    }

    // GET ITEMS
    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('stock_opname.show')")
    public ResponseEntity<List<StockOpnameItemResponse>> getItems(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-opnames/{}/items", id);
        return ResponseEntity.ok(stockOpnameService.findItems(id));
    }

    // STEP 1: CREATE — buat sesi opname (DRAFT)
    @PostMapping
    @PreAuthorize("hasAuthority('stock_opname.store')")
    public ResponseEntity<StockOpnameResponse> store(
            @Valid @RequestBody StockOpnameRequest request) {
        log.info("[POST] /api/v1/stock-opnames locationType={} locationId={}",
                request.getLocationType(), request.getLocationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockOpnameService.create(request));
    }

    // STEP 2: START COUNTING — snapshot qty_system, status → COUNTING
    @PatchMapping("/{id}/start-counting")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<StockOpnameResponse> startCounting(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/stock-opnames/{}/start-counting", id);
        return ResponseEntity.ok(stockOpnameService.startCounting(id));
    }

    // STEP 3: INPUT COUNTING — isi qty_physical, hitung selisih
    @PatchMapping("/{id}/count")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<List<StockOpnameItemResponse>> inputCounting(
            @PathVariable Long id,
            @Valid @RequestBody StockOpnameCountRequest request) {
        log.info("[PATCH] /api/v1/stock-opnames/{}/count", id);
        return ResponseEntity.ok(stockOpnameService.inputCounting(id, request));
    }

    // STEP 4: SUBMIT REVIEW — status → REVIEWED
    @PatchMapping("/{id}/submit-review")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<StockOpnameResponse> submitReview(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/stock-opnames/{}/submit-review", id);
        return ResponseEntity.ok(stockOpnameService.submitReview(id));
    }

    // STEP 5: APPROVE — status → APPROVED
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<StockOpnameResponse> approve(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/stock-opnames/{}/approve", id);
        return ResponseEntity.ok(stockOpnameService.approve(id));
    }

    // STEP 6: ADJUST — apply koreksi stock, status → ADJUSTED
    @PatchMapping("/{id}/adjust")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<StockOpnameResponse> adjust(@PathVariable Long id) {
        log.info("[PATCH] /api/v1/stock-opnames/{}/adjust", id);
        return ResponseEntity.ok(stockOpnameService.adjust(id));
    }
}