package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockBalanceRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockBalanceService {

    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;

    // GET ALL
    public List<StockBalance> findAll() {
        return stockBalanceRepository.findAll(Sort.by("id").ascending());
    }

    // GET ALL PAGINATED
    public Page<StockBalance> findAll(int page, int size) {
        return stockBalanceRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    // GET BY ID
    public StockBalance findById(Long id) {
        return stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));
    }

    // GET BY PRODUCT
    public List<StockBalance> findByProductId(Long productId) {
        return stockBalanceRepository.findByProductId(productId);
    }

    // GET BY LOCATION
    public List<StockBalance> findByLocation(String locationType, Long locationId) {
        return stockBalanceRepository.findByLocationTypeAndLocationId(locationType, locationId);
    }

    // CREATE
    public StockBalance create(StockBalanceRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        // Cek apakah sudah ada balance untuk kombinasi ini
        stockBalanceRepository.findByProductIdAndLocationTypeAndLocationId(
                request.getProductId(), request.getLocationType(), request.getLocationId()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException("Stock balance already exists for this product and location");
        });

        StockBalance stock = new StockBalance();
        stock.setProduct(product);
        stock.setLocationType(request.getLocationType());
        stock.setLocationId(request.getLocationId());
        stock.setQty(request.getQty());

        return stockBalanceRepository.save(stock);
    }

    // UPDATE QTY
    public StockBalance update(Long id, StockBalanceRequestDTO request) {
        StockBalance stock = stockBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockBalance", id));

        if (request.getQty() != null) stock.setQty(request.getQty());
        if (request.getLocationType() != null) stock.setLocationType(request.getLocationType());
        if (request.getLocationId() != null) stock.setLocationId(request.getLocationId());

        return stockBalanceRepository.save(stock);
    }

    // DELETE
    public void delete(Long id) {
        if (!stockBalanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("StockBalance", id);
        }
        stockBalanceRepository.deleteById(id);
    }
}