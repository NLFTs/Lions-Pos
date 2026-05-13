package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.service.inventory.StockBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/stock-balances")
@RequiredArgsConstructor
public class StockBalanceController {

    private final StockBalanceService stockBalanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockBalance>> index() {
        log.info("[GET] /api/v1/stock-balances");
        return ResponseEntity.ok(stockBalanceService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<Page<StockBalance>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-balances/page page={} size={}", page, size);
        return ResponseEntity.ok(stockBalanceService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_balance.show')")
    public ResponseEntity<StockBalance> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-balances/{}", id);
        return ResponseEntity.ok(stockBalanceService.findById(id));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockBalance>> getByProduct(@PathVariable Long productId) {
        log.info("[GET] /api/v1/stock-balances/product/{}", productId);
        return ResponseEntity.ok(stockBalanceService.findByProductId(productId));
    }

    @GetMapping("/location")
    @PreAuthorize("hasAuthority('stock_balance.index')")
    public ResponseEntity<List<StockBalance>> getByLocation(
            @RequestParam String locationType,
            @RequestParam Long locationId) {
        log.info("[GET] /api/v1/stock-balances/location type={} id={}", locationType, locationId);
        return ResponseEntity.ok(stockBalanceService.findByLocation(locationType, locationId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('stock_balance.store')")
    public ResponseEntity<StockBalance> store(@Valid @RequestBody StockBalanceRequestDTO request) {
        log.info("[POST] /api/v1/stock-balances productId={}", request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(stockBalanceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_balance.update')")
    public ResponseEntity<StockBalance> update(
            @PathVariable Long id,
            @Valid @RequestBody StockBalanceRequestDTO request) {
        log.info("[PUT] /api/v1/stock-balances/{}", id);
        return ResponseEntity.ok(stockBalanceService.update(id, request));
    }
}