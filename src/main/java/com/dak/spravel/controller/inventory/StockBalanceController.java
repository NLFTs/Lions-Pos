package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceInitRequest;
import com.dak.spravel.dto.response.inventoryresponse.StockLocationSummaryResponse;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.service.inventory.StockBalanceService;
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
@RequestMapping("/api/v1/stock-balances")
@RequiredArgsConstructor
public class StockBalanceController {

    private final StockBalanceService stockBalanceService;

    // SUPER ADMIN ONLY
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockBalance>> getAllForAdmin() {
        log.info("[GET] /api/v1/stock-balances/admin");
        return ResponseEntity.ok(stockBalanceService.findAllStockBalances());
    }

    // PARTNER / EMPLOYEE — semua stock (flat)
    @GetMapping
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockBalance>> index() {
        log.info("[GET] /api/v1/stock-balances");
        return ResponseEntity.ok(stockBalanceService.findAll());
    }

    // PAGINATION
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<Page<StockBalance>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-balances/page page={} size={}", page, size);
        return ResponseEntity.ok(stockBalanceService.findAll(page, size));
    }

    // SUMMARY — per product, dikelompokkan per lokasi + total qty
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockLocationSummaryResponse>> summary() {
        log.info("[GET] /api/v1/stock-balances/summary");
        return ResponseEntity.ok(stockBalanceService.findStockSummary());
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_balance.show')")
    public ResponseEntity<StockBalance> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-balances/{}", id);
        return ResponseEntity.ok(stockBalanceService.findById(id));
    }

    // GET BY LOKASI TERTENTU (branch/warehouse)
    @GetMapping("/location")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockBalance>> getByLocation(
            @RequestParam String locationType,
            @RequestParam Long locationId) {
        log.info("[GET] /api/v1/stock-balances/location type={} id={}", locationType, locationId);
        return ResponseEntity.ok(stockBalanceService.findByLocation(locationType, locationId));
    }

    // INISIASI STOCK AWAL
    @PostMapping("/initialize")
    @PreAuthorize("hasAuthority('stock_balance.store')")
    public ResponseEntity<List<StockBalance>> initialize(
            @Valid @RequestBody StockBalanceInitRequest request) {
        log.info("[POST] /api/v1/stock-balances/initialize locationType={} locationId={}",
                request.getLocationType(), request.getLocationId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stockBalanceService.initializeStock(request));
    }
}