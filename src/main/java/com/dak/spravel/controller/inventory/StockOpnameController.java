package com.dak.spravel.controller.inventory;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.dak.spravel.dto.request.inventory.StockOpnameRequestDTO;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.inventoryresponse.StockOpnameResponse;
import com.dak.spravel.model.inventory.StockOpname;
import com.dak.spravel.service.inventory.StockOpnameService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/stock-opnames")
@RequiredArgsConstructor
public class StockOpnameController {

    private final StockOpnameService stockOpnameService;

    @GetMapping
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<ResData<List<StockOpnameResponse>>> getAll() {
        log.info("[GET] /api/v1/stock-opnames - Fetching all stock opnames");
        return ResponseBuilder.ok(stockOpnameService.findAll());
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<ResData<List<StockOpnameResponse>>> getAllForAdmin() {
        log.info("[GET] /api/v1/stock-opnames/admin - Superadmin access fetching all partner data");
        // Update di sini panggil findAll()
        return ResponseBuilder.ok(stockOpnameService.findAll()); 
    }

    @GetMapping("/admin/page")
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<ResData<Page<StockOpnameResponse>>> paginatedAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-opnames/admin/page page={} size={}", page, size);
        return ResponseBuilder.ok(stockOpnameService.findPageAdmin(page, size));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_opname.index')")
    public ResponseEntity<ResData<Page<StockOpnameResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-opnames/page page={} size={}", page, size);
        return ResponseBuilder.ok(stockOpnameService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_opname.show')")
    public ResponseEntity<ResData<StockOpnameResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-opnames/{}", id);
        return ResponseBuilder.ok(stockOpnameService.findById(id));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('stock_opname.show')")
    public ResponseEntity<ResData<List<StockOpnameResponse.StockOpnameItemResponse>>> getItems(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-opnames/{}/items", id);
        return ResponseBuilder.ok(stockOpnameService.findItemsByOpnameId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('stock_opname.store')")
    public ResponseEntity<ResData<StockOpnameResponse>> store(@Valid @RequestBody StockOpnameRequestDTO request) {
        log.info("[POST] /api/v1/stock-opnames location={}", request.getLocationType());
        return ResponseBuilder.created(stockOpnameService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<ResData<StockOpname>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("[PATCH] /api/v1/stock-opnames/{}/status status={}", id, status);
        return ResponseBuilder.ok(
                stockOpnameService.updateStatus(id, status)
        );
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('stock_opname.update')")
    public ResponseEntity<ResData<StockOpname>> approve(@PathVariable Long id) {
        log.info("[POST] /api/v1/stock-opnames/{}/approve", id);
        return ResponseBuilder.ok(
                stockOpnameService.updateStatus(id, "approved")
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_opname.delete')")
    public ResponseEntity<Void> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/stock-opnames/{}", id);
        stockOpnameService.delete(id);
        return ResponseEntity.noContent().build();
    }
}