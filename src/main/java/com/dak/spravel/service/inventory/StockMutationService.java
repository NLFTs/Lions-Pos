package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.StockMutationRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.StockMutation;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.StockMutationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMutationService {

    private final StockMutationRepository stockMutationRepository;
    private final ProductRepository productRepository;
    private final PartnerRepository partnerRepository;

    // GET ALL
    public List<StockMutation> findAll() {
        return stockMutationRepository.findAll(Sort.by("createdAt").descending());
    }

    // GET ALL PAGINATED
    public Page<StockMutation> findAll(int page, int size) {
        return stockMutationRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // GET BY ID
    public StockMutation findById(Long id) {
        return stockMutationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMutation", id));
    }

    // GET BY PRODUCT
    public List<StockMutation> findByProductId(Long productId) {
        return stockMutationRepository.findByProductId(productId);
    }

    // GET BY PARTNER
    public List<StockMutation> findByPartnerId(Long partnerId) {
        return stockMutationRepository.findByPartnerId(partnerId);
    }

    // CREATE
    public StockMutation create(StockMutationRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        Partners partner = partnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        StockMutation mutation = new StockMutation();
        mutation.setProduct(product);
        mutation.setPartner(partner);
        mutation.setType(request.getType());
        mutation.setFromLocationType(request.getFromLocationType());
        mutation.setFromLocationId(request.getFromLocationId());
        mutation.setToLocationType(request.getToLocationType());
        mutation.setToLocationId(request.getToLocationId());
        mutation.setQty(request.getQty());
        mutation.setReferenceType(request.getReferenceType());
        mutation.setReferenceId(request.getReferenceId());
        mutation.setNotes(request.getNotes());

        return stockMutationRepository.save(mutation);
    }

    // DELETE (mutation tidak di update, hanya bisa dihapus)
    public void delete(Long id) {
        if (!stockMutationRepository.existsById(id)) {
            throw new ResourceNotFoundException("StockMutation", id);
        }
        stockMutationRepository.deleteById(id);
    }
}