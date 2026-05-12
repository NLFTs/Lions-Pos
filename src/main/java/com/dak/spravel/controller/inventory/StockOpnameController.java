package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.request.inventory.StockOpnameRequestDTO;
import com.dak.spravel.model.inventory.StockOpname;
import com.dak.spravel.model.inventory.StockOpnameItem;
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

    @GetMapping
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<List<StockOpname>> index() {
        log.info("[GET] /api/v1/stock-opnames");
        return ResponseEntity.ok(stockOpnameService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<Page<StockOpname>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-opnames/page page={} size={}", page, size);
        return ResponseEntity.ok(stockOpnameService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_opname.show')")
    public ResponseEntity<StockOpname> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-opnames/{}", id);
        return ResponseEntity.ok(stockOpnameService.findById(id));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('stock_opname.show')")
    public ResponseEntity<List<StockOpnameItem>> getItems(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-opnames/{}/items", id);
        return ResponseEntity.ok(stockOpnameService.findItemsByOpnameId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('stock_opname.store')")
    public ResponseEntity<StockOpname> store(@Valid @RequestBody StockOpnameRequestDTO request) {
        log.info("[POST] /api/v1/stock-opnames partnerId={}", request.getPartnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(stockOpnameService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<StockOpname> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("[PATCH] /api/v1/stock-opnames/{}/status status={}", id, status);
        return ResponseEntity.ok(stockOpnameService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_opname.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/stock-opnames/{}", id);
        stockOpnameService.delete(id);
        return ResponseEntity.noContent().build();
    }
}