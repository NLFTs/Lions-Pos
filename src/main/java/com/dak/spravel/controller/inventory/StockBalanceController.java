package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.BranchStockInRequest;
import com.dak.spravel.dto.request.inventory.WarehouseStockInRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceInitRequest;
import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.dto.request.inventory.StockTransferRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.inventoryresponse.InventoryDashboardResponse;
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

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<InventoryDashboardResponse>> getStats(
            @RequestParam(required = false) String locationType,
            @RequestParam(required = false) Long locationId) {
        log.info("[GET] /api/v1/stock-balances/stats locationType={} locationId={}", locationType, locationId);
        return ResponseBuilder.ok(stockBalanceService.getInventoryStats(locationType, locationId));
    }

    // SUPER ADMIN ONLY
    @GetMapping("/admin")
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

    @PostMapping("/warehouse")
    @PreAuthorize("hasAuthority('stock_balance.store')")
    public ResponseEntity<ResData<StockBalanceResponse>> storeFromWarehouse(
            @Valid @RequestBody WarehouseStockInRequest request) {
        log.info("[POST] /api/v1/stock-balances/warehouse warehouseId={} productId={} qty={}",
                request.getWarehouseId(), request.getProductId(), request.getQty());
        return ResponseBuilder.ok(stockBalanceService.createFromWarehouse(request));
    }

    @PostMapping("/branch")
    @PreAuthorize("hasAuthority('stock_balance.store')")
    public ResponseEntity<ResData<StockBalanceResponse>> storeFromBranch(
            @Valid @RequestBody BranchStockInRequest request) {
        log.info("[POST] /api/v1/stock-balances/branch branchId={} productId={} qty={}",
                request.getBranchId(), request.getProductId(), request.getQty());
        return ResponseBuilder.ok(stockBalanceService.createFromBranch(request));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('stock_balance.transfer')")
    public ResponseEntity<ResData<StockBalanceResponse>> transfer(
            @Valid @RequestBody StockTransferRequest request) {
        log.info("[POST] /api/v1/stock-balances/transfer fromLocationType={} fromLocationId={} toLocationType={} toLocationId={} qty={}",
                request.getFromLocationType(), request.getFromLocationId(), request.getToLocationType(), request.getToLocationId(), request.getQty());
        return ResponseBuilder.ok(stockBalanceService.transferStock(request));
    }

    @GetMapping("/quarantine")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<ResData<List<StockBalanceResponse>>> getQuarantine() {
        log.info("[GET] /api/v1/stock-balances/quarantine");
        return ResponseBuilder.ok(stockBalanceService.findQuarantineStock());
    }

    @PostMapping("/{id}/dispose")
    @PreAuthorize("hasAuthority('stock_balance.update')")
    public ResponseEntity<ResData<StockBalanceResponse>> dispose(
            @PathVariable Long id,
            @RequestParam(required = false) Long qty,
            @RequestParam(required = false) String notes) {
        log.info("[POST] /api/v1/stock-balances/{}/dispose qty={} notes={}", id, qty, notes);
        return ResponseBuilder.ok(stockBalanceService.disposeQuarantine(id, qty, notes));
    }
}       