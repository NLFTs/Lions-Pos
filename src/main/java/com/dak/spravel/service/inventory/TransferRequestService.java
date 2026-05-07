package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.TransferRequestDTO;
import com.dak.spravel.dto.request.inventory.TransferRequestItemDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.TransferRequest;
import com.dak.spravel.model.inventory.TransferRequestItem;
import com.dak.spravel.repository.catalog.ProductRepository;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.TransferRequestRepository;
import com.dak.spravel.repository.inventory.TransferRequestItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final TransferRequestItemRepository transferRequestItemRepository;
    private final PartnerRepository partnersRepository;
    private final ProductRepository productRepository;

    // GET ALL
    public List<TransferRequest> findAll() {
        return transferRequestRepository.findByDeletedAtIsNull();
    }

    // GET ALL PAGINATED
    public Page<TransferRequest> findAll(int page, int size) {
        return transferRequestRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // GET BY ID
    public TransferRequest findById(Long id) {
        return transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));
    }

    // GET BY PARTNER
    public List<TransferRequest> findByPartnerId(Long partnerId) {
        return transferRequestRepository.findByPartnerIdAndDeletedAtIsNull(partnerId);
    }

    // CREATE
    @Transactional
    public TransferRequest create(TransferRequestDTO request) {
        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setPartner(partner);
        transferRequest.setFromLocationType(request.getFromLocationType());
        transferRequest.setFromLocationId(request.getFromLocationId());
        transferRequest.setToLocationType(request.getToLocationType());
        transferRequest.setToLocationId(request.getToLocationId());
        transferRequest.setNotes(request.getNotes());
        transferRequest.setStatus(TransferRequest.Status.PENDING);
        transferRequest.setRequestedAt(LocalDateTime.now());

        TransferRequest saved = transferRequestRepository.save(transferRequest);

        // Save items
        List<TransferRequestItem> items = new ArrayList<>();
        for (TransferRequestItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

            TransferRequestItem item = new TransferRequestItem();
            item.setTransferRequest(saved);
            item.setProduct(product);
            item.setQtyRequested(itemDTO.getQtyRequested());
            items.add(item);
        }
        transferRequestItemRepository.saveAll(items);
        saved.setItems(items);

        return saved;
    }

    // UPDATE STATUS
    @Transactional
    public TransferRequest updateStatus(Long id, String status) {
        TransferRequest transferRequest = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        TransferRequest.Status newStatus = TransferRequest.Status.valueOf(status.toUpperCase());
        transferRequest.setStatus(newStatus);

        if (newStatus == TransferRequest.Status.APPROVED) {
            transferRequest.setApprovedAt(LocalDateTime.now());
        } else if (newStatus == TransferRequest.Status.RECEIVED) {
            transferRequest.setReceivedAt(LocalDateTime.now());
        }

        return transferRequestRepository.save(transferRequest);
    }

    // SOFT DELETE
    public void delete(Long id) {
        TransferRequest transferRequest = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequest", id));

        transferRequest.setDeletedAt(LocalDateTime.now());
        transferRequestRepository.save(transferRequest);
    }
}