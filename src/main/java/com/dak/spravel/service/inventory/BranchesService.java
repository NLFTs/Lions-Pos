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

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchesService {

    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Branch.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private Branches getValidatedBranch(Long id, User currentUser) {
        Branches branch = branchesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        if (currentUser.getPartner() == null ||
                !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses branch dari partner lain.");
        }

        return branch;
    }

    // GET ALL
    public List<Branches> findAll() {
        User currentUser = getAuthenticatedUser();
        return branchesRepository.findByPartnersId(currentUser.getPartner().getId());
    }

    // GET ALL PAGINATED
    public Page<Branches> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        return branchesRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    // GET BY ID
    public Branches findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedBranch(id, currentUser);
    }

    // CREATE
    public Branches create(BranchesRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        Branches branch = new Branches();
        branch.setPartners(partner);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());

        return branchesRepository.save(branch);
    }

    // UPDATE
    public Branches update(Long id, BranchesRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        Branches branch = getValidatedBranch(id, currentUser);

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());

        return branchesRepository.save(branch);
    }

    // DELETE
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Branches branch = getValidatedBranch(id, currentUser);
        branchesRepository.delete(branch);
    }
}