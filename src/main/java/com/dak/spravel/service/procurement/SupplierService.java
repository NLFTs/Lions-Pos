package com.dak.spravel.service.procurement;

import com.dak.spravel.dto.request.procurement.SupplierRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.procurement.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final PartnerRepository partnersRepository;

    public List<Supplier> findAll() {
        return supplierRepository.findByDeletedAtIsNull();
    }

    public Page<Supplier> findAll(int page, int size) {
        return supplierRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }

    public Supplier create(SupplierRequestDTO request) {
        Partners partner = partnersRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnerId()));

        Supplier supplier = new Supplier();
        supplier.setPartner(partner);
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setNotes(request.getNotes());

        return supplierRepository.save(supplier);
    }

    public Supplier update(Long id, SupplierRequestDTO request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        if (request.getName() != null) supplier.setName(request.getName());
        if (request.getPhone() != null) supplier.setPhone(request.getPhone());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());
        if (request.getNotes() != null) supplier.setNotes(request.getNotes());

        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setDeletedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }
}