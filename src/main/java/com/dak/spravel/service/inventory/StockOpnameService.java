package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockOpnameItemDTO;
import com.dak.spravel.dto.request.inventory.StockOpnameRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.StockOpname;
import com.dak.spravel.model.inventory.StockOpnameItem;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.StockOpnameItemRepository;
import com.dak.spravel.repository.inventory.StockOpnameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockOpnameService {

    private final StockOpnameRepository stockOpnameRepository;
    private final StockOpnameItemRepository stockOpnameItemRepository;
    private final PartnerRepository partnersRepository;
    private final ProductRepository productRepository;

    // GET ALL
    public List<StockOpname> findAll() {
        return stockOpnameRepository.findByDeletedAtIsNull();
    }

    // GET ALL PAGINATED
    public Page<StockOpname> findAll(int page, int size) {
        return stockOpnameRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // GET BY ID
    public StockOpname findById(Long id) {
        return stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));
    }

    // GET ITEMS BY OPNAME ID
    public List<StockOpnameItem> findItemsByOpnameId(Long opnameId) {
        return stockOpnameItemRepository.findByStockOpnameId(opnameId);
    }

    // CREATE
    @Transactional
    public StockOpname create(StockOpnameRequestDTO request) {
        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        StockOpname opname = new StockOpname();
        opname.setPartner(partner);
        opname.setLocation(request.getLocation());
        opname.setLocationId(request.getLocationId());
        opname.setDate(request.getDate());
        opname.setNotes(request.getNotes());
        opname.setStatus(StockOpname.Status.DRAFT);

        StockOpname saved = stockOpnameRepository.save(opname);

        // Save items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<StockOpnameItem> items = new ArrayList<>();
            for (StockOpnameItemDTO itemDTO : request.getItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

                StockOpnameItem item = new StockOpnameItem();
                item.setStockOpname(saved);
                item.setProduct(product);
                item.setQtySystem(itemDTO.getQtySystem() != null ? itemDTO.getQtySystem() : BigDecimal.ZERO);
                item.setQtyPhysical(itemDTO.getQtyPhysical() != null ? itemDTO.getQtyPhysical() : BigDecimal.ZERO);
                item.setQtyDifference(item.getQtyPhysical().subtract(item.getQtySystem()));
                item.setNotes(itemDTO.getNotes());
                items.add(item);
            }
            stockOpnameItemRepository.saveAll(items);
        }

        return saved;
    }

    // UPDATE STATUS
    @Transactional
    public StockOpname updateStatus(Long id, String status) {
        StockOpname opname = stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));

        StockOpname.Status newStatus = StockOpname.Status.valueOf(status.toUpperCase());
        opname.setStatus(newStatus);

        if (newStatus == StockOpname.Status.RIEVIEWED) {
            opname.setReviewedAt(LocalDateTime.now());
        } else if (newStatus == StockOpname.Status.APPROVED) {
            opname.setApprovedAt(LocalDateTime.now());
        }

        return stockOpnameRepository.save(opname);
    }

    // SOFT DELETE
    public void delete(Long id) {
        StockOpname opname = stockOpnameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockOpname", id));

        opname.setDeletedAt(LocalDateTime.now());
        stockOpnameRepository.save(opname);
    }
}