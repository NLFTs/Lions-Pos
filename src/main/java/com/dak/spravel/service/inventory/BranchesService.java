package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.inventory.BranchesRequestDTO;
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
    public List<Branches> findAll() {
        User currentUser = getAuthenticatedUser();
        validatePartnerAccess(currentUser);

        // Langsung ambil berdasarkan objek partner yang nempel di user
        return branchesRepository.findByPartners(currentUser.getPartner());
    }

    // GET ALL PAGINATED
    public Page<Branches> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        validatePartnerAccess(currentUser);

        // Lebih efisien pake ID-nya aja buat pagination
        return branchesRepository.findByPartnersId(currentUser.getPartner().getId(), PageRequest.of(page, size, Sort.by("name")));
    }

    // GET BY ID
    public Branches findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedBranch(id, currentUser);
    }

    // CREATE
    @Transactional
    public Branches create(BranchesRequestDTO request) {
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

        return branchesRepository.save(branch);
    }

    // UPDATE
    @Transactional
    public Branches update(Long id, BranchesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        Branches branch = getValidatedBranch(id, currentUser);

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        
        // Tambahkan Auditing Update
        branch.setUpdatedBy(currentUser);
        // AuditHelper.setUpdated(branch); // Jika ada helpernya

        return branchesRepository.save(branch);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Branches branch = getValidatedBranch(id, currentUser);
        branchesRepository.delete(branch);
    }
}