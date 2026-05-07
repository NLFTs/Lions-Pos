package com.dak.spravel.service.procurement;

import com.dak.spravel.dto.request.procurement.SupplierRequestDTO;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.procurement.Supplier;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.procurement.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));

        if (isAdmin(user)) {
            throw new RuntimeException("Akses Ditolak: Admin tidak diperbolehkan mengelola Supplier.");
        }

        return user;
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("super_admin") || role.getSlug().equals("admin"));
    }

    private Supplier getValidatedSupplier(Long id, User currentUser) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        if (currentUser.getPartner() == null ||
                !supplier.getPartner().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Supplier bukan milik partner Anda.");
        }

        return supplier;
    }

    // GET ALL
    public List<Supplier> findAll() {
        User currentUser = getAuthenticatedUser();
        return supplierRepository.findByPartnerIdAndDeletedAtIsNull(currentUser.getPartner().getId());
    }

    // GET ALL PAGINATED
    public Page<Supplier> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        return supplierRepository.findAll(PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    // GET BY ID
    public Supplier findById(Long id) {
        User currentUser = getAuthenticatedUser();
        return getValidatedSupplier(id, currentUser);
    }

    // CREATE
    public Supplier create(SupplierRequestDTO request) {
        User currentUser = getAuthenticatedUser();

        Partners partner = currentUser.getPartner();
        if (partner == null) {
            throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
        }

        Supplier supplier = new Supplier();
        supplier.setPartner(partner);
        supplier.setName(request.getName());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setNotes(request.getNotes());

        return supplierRepository.save(supplier);
    }

    // UPDATE
    public Supplier update(Long id, SupplierRequestDTO request) {
        User currentUser = getAuthenticatedUser();
        Supplier supplier = getValidatedSupplier(id, currentUser);

        if (request.getName() != null) supplier.setName(request.getName());
        if (request.getPhone() != null) supplier.setPhone(request.getPhone());
        if (request.getAddress() != null) supplier.setAddress(request.getAddress());
        if (request.getNotes() != null) supplier.setNotes(request.getNotes());

        return supplierRepository.save(supplier);
    }

    // SOFT DELETE
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        Supplier supplier = getValidatedSupplier(id, currentUser);
        supplier.setDeletedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }
}