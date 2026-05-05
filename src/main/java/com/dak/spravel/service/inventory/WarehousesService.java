package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.WarehousesRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Warehouses;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.WarehousesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehousesService {

    private final WarehousesRepository warehousesRepository;
    private final PartnerRepository partnersRepository;

    // GET ALL
    public List<Warehouses> findAll() {
        return warehousesRepository.findAll(Sort.by("name").ascending());
    }

    // GET ALL PAGINATED
    public Page<Warehouses> findAll(int page, int size) {
        return warehousesRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    // GET BY ID
    public Warehouses findById(Long id) {
        return warehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    // CREATE
    public Warehouses create(WarehousesRequestDTO request) {
        Partners partners = partnersRepository.findById(request.getPartnersId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnersId()));

        Warehouses warehouse = new Warehouses();
        warehouse.setPartners(partners);
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return warehousesRepository.save(warehouse);
    }

    // UPDATE
    public Warehouses update(Long id, WarehousesRequestDTO request) {
        Warehouses warehouse = warehousesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));

        Partners partners = partnersRepository.findById(request.getPartnersId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnersId()));

        warehouse.setPartners(partners);
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        if (request.getIsActive() != null) warehouse.setIsActive(request.getIsActive());

        return warehousesRepository.save(warehouse);
    }

    // DELETE
    public void delete(Long id) {
        if (!warehousesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Warehouse", id);
        }
        warehousesRepository.deleteById(id);
    }
}