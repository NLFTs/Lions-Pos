package com.dak.spravel.controller.inventory;

import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.inventoryresponse.StockMutationResponse;
import com.dak.spravel.service.inventory.StockMutationService;
import com.dak.spravel.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/stock-mutations")
@RequiredArgsConstructor
public class StockMutationController {

    private final StockMutationService stockMutationService;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<ResData<Page<StockMutationResponse>>> getAllForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-mutations/admin - Superadmin access, page: {}, size: {}", page, size);
        return ResponseBuilder.ok(stockMutationService.findAll(page, size));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<ResData<List<StockMutationResponse>>> index() {
        log.info("[GET] /api/v1/stock-mutations");
        return ResponseBuilder.ok(stockMutationService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<ResData<Page<StockMutationResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-mutations/page page={} size={}", page, size);
        return ResponseBuilder.ok(stockMutationService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_mutation.show')")
    public ResponseEntity<ResData<StockMutationResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-mutations/{}", id);
        return ResponseBuilder.ok(stockMutationService.findById(id));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<ResData<List<StockMutationResponse>>> getByProduct(@PathVariable Long productId) {
        log.info("[GET] /api/v1/stock-mutations/product/{}", productId);
        return ResponseBuilder.ok(stockMutationService.findByProductId(productId));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<ResData<List<StockMutationResponse>>> getByPartner(@PathVariable Long partnerId) {
        log.info("[GET] /api/v1/stock-mutations/partner/{}", partnerId);
        return ResponseBuilder.ok(stockMutationService.findByPartnerId(partnerId));
    }

    
}