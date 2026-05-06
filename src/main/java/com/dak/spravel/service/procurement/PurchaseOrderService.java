package com.dak.spravel.service.procurement;

import com.dak.spravel.dto.request.procurement.PurchaseOrderItemDTO;
import com.dak.spravel.dto.request.procurement.PurchaseOrderRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.procurement.PurchaseOrder;
import com.dak.spravel.model.procurement.PurchaseOrderItems;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderItemsRepository;
import com.dak.spravel.repository.procurement.PurchaseOrderRepository;
import com.dak.spravel.repository.procurement.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemsRepository purchaseOrderItemsRepository;
    private final PartnerRepository partnersRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findByDeletedAtIsNull();
    }

    public Page<PurchaseOrder> findAll(int page, int size) {
        return purchaseOrderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    public List<PurchaseOrderItems> findItemsByOrderId(Long orderId) {
        return purchaseOrderItemsRepository.findByPurchaseOrderId(orderId);
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderRequestDTO request) {
        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setPartner(partner);
        po.setSupplier(supplier);
        po.setPoNumber(generatePoNumber());
        po.setLocationType(request.getLocationType());
        po.setLocationId(request.getLocationId());
        po.setOrderDate(request.getOrderDate());
        po.setExpectedDate(request.getExpectedDate());
        po.setNotes(request.getNotes());
        po.setStatus(PurchaseOrder.Status.DRAFT);

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Save items
        List<PurchaseOrderItems> items = new ArrayList<>();
        for (PurchaseOrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

            PurchaseOrderItems item = new PurchaseOrderItems();
            item.setPurchaseOrder(saved);
            item.setProduct(product);
            item.setProductName(product.getName()); // snapshot
            item.setQtyOrdered(itemDTO.getQtyOrdered());
            item.setUnitCost(itemDTO.getUnitCost());
            item.setSubtotal(itemDTO.getQtyOrdered().multiply(itemDTO.getUnitCost()));
            items.add(item);
        }
        purchaseOrderItemsRepository.saveAll(items);

        // Update total
        saved.setTotal(items.stream()
                .map(PurchaseOrderItems::getSubtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        purchaseOrderRepository.save(saved);

        return saved;
    }

    public PurchaseOrder updateStatus(Long id, String status) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));

        po.setStatus(PurchaseOrder.Status.valueOf(status.toUpperCase()));
        return purchaseOrderRepository.save(po);
    }

    public void delete(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        po.setDeletedAt(LocalDateTime.now());
        purchaseOrderRepository.save(po);
    }

    private String generatePoNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PO-" + date + "-";
        long count = purchaseOrderRepository.countByPoNumberStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }
}