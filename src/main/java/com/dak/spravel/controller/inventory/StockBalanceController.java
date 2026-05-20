package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceInitRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.inventoryresponse.StockBalanceResponse;
import com.dak.spravel.dto.response.inventoryresponse.StockLocationSummaryResponse;
import com.dak.spravel.service.inventory.StockBalanceService;
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
@RequestMapping("/api/v1/stock-balances")
@RequiredArgsConstructor
public class StockBalanceController {

    private final StockBalanceService stockBalanceService;

    // SUPER ADMIN ONLY
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockBalanceResponse>> getAllForAdmin() {
        log.info("[GET] /api/v1/stock-balances/admin");
        return ResponseEntity.ok(stockBalanceService.findAllStockBalance());
    }

    // PARTNER / EMPLOYEE — semua stock flat
    @GetMapping
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<List<StockBalanceResponse>>> index() {
        log.info("[GET] /api/v1/stock-balances");
        return ResponseBuilder.ok(stockBalanceService.findAll());
    }

    // PAGINATION
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<Page<StockBalanceResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-balances/page page={} size={}", page, size);
        return ResponseBuilder.ok(stockBalanceService.findAll(page, size));
    }

    // SUMMARY — per product, dikelompokkan per lokasi + total qty
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<List<StockLocationSummaryResponse>>> summary() {
        log.info("[GET] /api/v1/stock-balances/summary");
        return ResponseBuilder.ok(stockBalanceService.findStockSummary());
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_balance.show')")
    public ResponseEntity<ResData<StockBalanceResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-balances/{}", id);
        return ResponseBuilder.ok(stockBalanceService.findById(id));
    }

    // GET BY LOKASI TERTENTU (branch/warehouse)
    @GetMapping("/location")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<List<StockBalanceResponse>>> getByLocation(
            @RequestParam String locationType,
            @RequestParam Long locationId) {
        log.info("[GET] /api/v1/stock-balances/location type={} id={}", locationType, locationId);
        return ResponseBuilder.ok(stockBalanceService.findByLocation(locationType, locationId));
    }

    // GET BY BRANCH
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<List<StockBalanceResponse>>> getByBranch(
            @PathVariable Long branchId) {
        log.info("[GET] /api/v1/stock-balances/branch/{}", branchId);
        return ResponseBuilder.ok(stockBalanceService.findByBranch(branchId));
    }

    // GET BY WAREHOUSE
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<List<StockBalanceResponse>>> getByWarehouse(
            @PathVariable Long warehouseId) {
        log.info("[GET] /api/v1/stock-balances/warehouse/{}", warehouseId);
        return ResponseBuilder.ok(stockBalanceService.findByWarehouse(warehouseId));
    }

    // CREATE — stock awal manual 1 produk
    @PostMapping
    @PreAuthorize("hasAuthority('stock_balance.store')")
    public ResponseEntity<ResData<StockBalanceResponse>> store(
            @Valid @RequestBody StockBalanceRequestDTO request) {
        log.info("[POST] /api/v1/stock-balances productId={} locationType={} locationId={}",
                request.getProduct(), request.getLocationType(), request.getLocationId());
        return ResponseBuilder.ok(stockBalanceService.create(request));
    }

    // INISIASI STOCK AWAL BATCH — banyak produk sekaligus
    @PostMapping("/initialize")
    @PreAuthorize("hasAuthority('stock_balance.store')")
    public ResponseEntity<ResData<List<StockBalanceResponse>>> initialize(
            @Valid @RequestBody StockBalanceInitRequest request) {
        log.info("[POST] /api/v1/stock-balances/initialize locationType={} locationId={}",
                request.getLocationType(), request.getLocationId());
        return ResponseBuilder.ok(stockBalanceService.initializeStock(request));
    }
}