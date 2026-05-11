package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.BranchesRequestDTO;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.BranchResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Role yang dibawa: " + auth.getAuthorities());
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_SUPER_ADMIN") || role.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Branch.");
        }
        
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private void validatePartnerAccess(User user) {
        // Cek apakah user punya partner
        if (user.getPartner() == null) {
            throw new RuntimeException("Akses Ditolak: Anda tidak terasosiasi dengan Partner manapun.");
        }
    }

    private Branches getValidatedBranch(Long id, User currentUser) {
        validatePartnerAccess(currentUser);
        
        Branches branch = branchesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        // Pastikan branch ini milik partner si user
        if (!branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses branch dari partner lain.");
        }

        return branch;
    }

    // GET ALL (Tanpa Pagination)
    public List<BranchResponse> findAll() {
        User currentUser = getAuthenticatedUser();
        validatePartnerAccess(currentUser);

        // Langsung ambil berdasarkan objek partner yang nempel di user
        return branchesRepository.findByPartners(currentUser.getPartner())                
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ALL PAGINATED
    public Page<BranchResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        validatePartnerAccess(currentUser);

        // Lebih efisien pake ID-nya aja buat pagination
        return branchesRepository.findByPartnersId(currentUser.getPartner().getId(), PageRequest.of(page, size, Sort.by("name").ascending()))
                .map(this::mapToResponse);
    }

    // GET BY ID
    public BranchResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return mapToResponse(getValidatedBranch(id, currentUser));
    }

    // CREATE
    @Transactional
    public BranchResponse create(BranchesRequestDTO request) {

        User currentUser = getAuthenticatedUser();
        
        validatePartnerAccess(currentUser);

        Partners partner = currentUser.getPartner();

        Branches branch = new Branches();
        branch.setPartners(partner);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        
        // Tambahkan Auditing
        branch.setCreatedBy(currentUser);
        // AuditHelper.setCreated(branch); // Jika lu mau pake helper

        return mapToResponse(branchesRepository.save(branch));
    }

    // UPDATE
    @Transactional
    public BranchResponse update(Long id, BranchesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        Branches branch = getValidatedBranch(id, currentUser);

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        
        // Tambahkan Auditing Update
        branch.setUpdatedBy(currentUser);
        // AuditHelper.setUpdated(branch); // Jika ada helpernya

        return mapToResponse(branchesRepository.save(branch));
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Branches branch = getValidatedBranch(id, currentUser);
        branchesRepository.delete(branch);
    }

    private BranchResponse mapToResponse(Branches branch) {
        BranchResponse response = new BranchResponse();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setAddress(branch.getAddress());

        if (branch.getPartners() != null) {
            PartnerSimpleDto pDto = new PartnerSimpleDto();
            pDto.setId(branch.getPartners().getId());
            pDto.setName(branch.getPartners().getName());
            response.setPartner(pDto);

        }

        response.setIsActive(branch.getIsActive());
        response.setCreatedAt(branch.getCreatedAt());
        response.setUpdatedAt(branch.getUpdatedAt());
        response.setCreatedBy(mapUserToDto(branch.getCreatedBy()));
        response.setUpdatedBy(mapUserToDto(branch.getUpdatedBy()));
        return response;
    }

    private UserSimpleDto mapUserToDto(User user) {
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user. getId());
        dto.setUsername(user.getUsername());
        return dto;
    }
}