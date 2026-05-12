package com.dak.spravel.controller.inventory;

import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.service.inventory.StockMutationService;
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
@RequestMapping("/api/v1/stock-mutations")
@RequiredArgsConstructor
public class StockMutationController {

    private final StockMutationService stockMutationService;

    @GetMapping
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<List<StockMutation>> index() {
        log.info("[GET] /api/v1/stock-mutations");
        return ResponseEntity.ok(stockMutationService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<Page<StockMutation>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("[GET] /api/v1/stock-mutations/page page={} size={}", page, size);
        return ResponseEntity.ok(stockMutationService.findAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('stock_mutation.show')")
    public ResponseEntity<StockMutation> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/stock-mutations/{}", id);
        return ResponseEntity.ok(stockMutationService.findById(id));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<List<StockMutation>> getByProduct(@PathVariable Long productId) {
        log.info("[GET] /api/v1/stock-mutations/product/{}", productId);
        return ResponseEntity.ok(stockMutationService.findByProductId(productId));
    }

    @GetMapping("/partner/{partnerId}")
    @PreAuthorize("hasAuthority('stock_mutation.index')")
    public ResponseEntity<List<StockMutation>> getByPartner(@PathVariable Long partnerId) {
        log.info("[GET] /api/v1/stock-mutations/partner/{}", partnerId);
        return ResponseEntity.ok(stockMutationService.findByPartnerId(partnerId));
    }

    
}