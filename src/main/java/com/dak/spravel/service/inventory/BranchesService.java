package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.BranchesRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.repository.common.PartnerRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final PartnerRepository partnersRepository;

    // GET ALL
    public List<Branches> findAll() {
        return branchesRepository.findAll(Sort.by("name").ascending());
    }

    // GET ALL PAGINATED
    public Page<Branches> findAll(int page, int size) {
        return branchesRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    // GET BY ID
    public Branches findById(Long id) {
        return branchesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
    }

    // CREATE
    public Branches create(BranchesRequestDTO request) {
        Partners partners = partnersRepository.findById(request.getPartnersId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnersId()));

        Branches branch = new Branches();
        branch.setPartners(partners);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return branchesRepository.save(branch);
    }

    // UPDATE
    public Branches update(Long id, BranchesRequestDTO request) {
        Branches branch = branchesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        Partners partners = partnersRepository.findById(request.getPartnersId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner", request.getPartnersId()));

        branch.setPartners(partners);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        if (request.getIsActive() != null) branch.setIsActive(request.getIsActive());

        return branchesRepository.save(branch);
    }

    // DELETE
    public void delete(Long id) {
        if (!branchesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Branch", id);
        }
        branchesRepository.deleteById(id);
    }
}