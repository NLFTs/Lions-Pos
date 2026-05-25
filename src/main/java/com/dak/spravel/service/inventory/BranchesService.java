package com.dak.spravel.service.inventory;

import com.dak.spravel.dto.request.partner.BranchRequest;
import com.dak.spravel.dto.response.components.PartnerSimpleDto;
import com.dak.spravel.dto.response.components.UserSimpleDto;
import com.dak.spravel.dto.response.inventoryresponse.BranchResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.model.common.Partners;
import com.dak.spravel.model.inventory.Branches;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.repository.inventory.BranchesRepository;
import com.dak.spravel.util.AuditHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.inventory.StockBalance;
import com.dak.spravel.model.catalog.Product;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.inventory.StockBalanceRepository;
import com.dak.spravel.repository.catalog.ProductRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class BranchesService {
 
    private final BranchesRepository branchesRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockBalanceRepository stockBalanceRepository;
    private final ProductRepository productRepository;


    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("User tidak terautentikasi");
        }

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database"));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equals("admin") ||
                                role.getSlug().equals("super_admin")
                );
    }

    private User getAuthenticatedSuperAdmin() {
        User user = getAuthenticatedUser();

        boolean isSuperAdmin = isAdmin(user);
        if (!isSuperAdmin) {
            throw new RuntimeException("Akses ditolak: Anda bukan Super Admin");
        }

        return user;
    }


    private User getAuthenticatedAdminPartnerOrEmployee() {
        User user = getAuthenticatedUser();

        boolean isAuthorized = user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equalsIgnoreCase("admin-partners") ||
                                role.getSlug().equalsIgnoreCase("employee-partners") ||
                                role.getSlug().equalsIgnoreCase("owner") ||
                                role.getSlug().equalsIgnoreCase("employee")
                );

        boolean isNotSuperAdmin = user.getRoles().stream()
                .noneMatch(role -> role.getSlug().equalsIgnoreCase("admin"));

        if (!isAuthorized || !isNotSuperAdmin) {
            throw new RuntimeException("Akses Ditolak: Hanya Admin Partner atau Employee yang diizinkan.");
        }

        return user;
    }
    
    private boolean isEmployee(User user) {
        return user.getRoles().stream()
                .anyMatch(role ->
                        role.getSlug().equalsIgnoreCase("employee") ||
                                role.getSlug().equalsIgnoreCase("employee-partners")
                );
    }

    private Branches getValidatedBranch(Long id, User currentUser) {

        Branches branch = branchesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        if (currentUser.getPartner() == null || branch.getPartners() == null || !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
            throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses branch partner lain.");
        }

        return branch;
    }

    public List<BranchResponse> findAllBranches() {

        getAuthenticatedSuperAdmin();

        List<Branches> branches = branchesRepository.findAll();

        return branches.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<BranchResponse> findPageAdmin(int page, int size) {
        getAuthenticatedSuperAdmin();
        
        return branchesRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    public List<BranchResponse> findAll() {

        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        return branchesRepository.findByPartners(currentUser.getPartner())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    

    public Page<BranchResponse> findAll(int page, int size) {
        User currentUser = getAuthenticatedUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").ascending());

        if (isAdmin(currentUser)) {
            return branchesRepository.findAll(pageRequest).map(this::mapToResponse);
        }

        if (currentUser.getPartner() == null) {
            throw new RuntimeException("User tidak terasosiasi dengan Partner.");
        }

        return branchesRepository.findByPartnersId(currentUser.getPartner().getId(), pageRequest)
                .map(this::mapToResponse);
    }


    public BranchResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        Branches branch = branchesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));

        if (!isAdmin(currentUser)) {
            if (currentUser.getPartner() == null || branch.getPartners() == null || !branch.getPartners().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: Anda tidak bisa mengakses branch partner lain.");
            }
        }

        return mapToResponse(branch);
    }


    @Transactional
    public BranchResponse create(BranchRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        Partners partner = currentUser.getPartner();

        if (partner == null) {
            throw new RuntimeException("User tidak terasosiasi dengan partner manapun.");
        }

        if (isEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak dapat menambah Cabang baru."
            );
        }

        Branches branch = new Branches();

        branch.setPartners(partner);
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setIsActive(true);
        branch.setCreatedAt(LocalDateTime.now());
        branch.setCreatedBy(currentUser);

        Branches savedBranch = branchesRepository.save(branch);

        // Create user if username and password are provided
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String username = request.getUsername().trim();
            if (userRepository.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Username '" + username + "' sudah digunakan.");
            }

            User branchUser = new User();
            branchUser.setUsername(username);
            branchUser.setFullname("Cabang " + savedBranch.getName());
            branchUser.setEmail(username + "@gaptek.com");
            branchUser.setPassword(passwordEncoder.encode(request.getPassword()));
            branchUser.setPartner(partner);
            branchUser.setBranch(savedBranch);

            Role employeeRole = roleRepository.findBySlugAndPartnerId("employee-partners", partner.getId())
                    .orElseThrow(() -> new RuntimeException("Role employee-partners tidak ditemukan"));
            Set<Role> roles = new HashSet<>();
            roles.add(employeeRole);
            branchUser.setRoles(roles);

            userRepository.save(branchUser);
        }

        // Inisialisasi Stock Balance untuk semua produk partner di branch baru
        List<Product> products = productRepository.findAllByPartner(partner);
        for (Product product : products) {
            StockBalance stock = new StockBalance();
            stock.setProduct(product);
            stock.setLocationType("BRANCH");
            stock.setLocationId(savedBranch.getId());
            stock.setQty(0L);
            stock.setCreatedAt(LocalDateTime.now());
            stock.setCreatedBy(currentUser);
            stock.setUpdatedBy(currentUser);
            stockBalanceRepository.save(stock);
        }

        return mapToResponse(savedBranch);
    }


    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (isEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak dapat mengubah data Cabang."
            );
        }

        Branches branch = getValidatedBranch(id, currentUser);

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setUpdatedBy(currentUser);

        AuditHelper.setUpdated(branch);

        return mapToResponse(branchesRepository.save(branch));
    }

    @Transactional
    public BranchResponse softDelete(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (isEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak dapat menghapus Cabang."
            );
        }

        Branches branch = getValidatedBranch(id, currentUser);

        branch.setIsActive(false);
        branch.setDeletedBy(currentUser);

        AuditHelper.setUpdated(branch);

        return mapToResponse(branchesRepository.save(branch));
    }

    @Transactional
    public BranchResponse restoreBranch(Long id) {
        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (isEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak dapat memulihkan Cabang."
            );
        }


        Branches branch = getValidatedBranch(id, currentUser);

        branch.setIsActive(true);
        branch.setUpdatedBy(currentUser);

        AuditHelper.setUpdated(branch);

        return mapToResponse(branchesRepository.save(branch));
    }

    @Transactional
    public void delete(Long id) {

        User currentUser = getAuthenticatedAdminPartnerOrEmployee();

        if (isEmployee(currentUser)) {
            throw new RuntimeException(
                    "Akses Ditolak: Employee tidak dapat menghapus permanen Cabang."
            );
        }

        Branches branch = getValidatedBranch(id, currentUser);

        branchesRepository.delete(branch);
    }

    private BranchResponse mapToResponse(Branches branch) {

        if (branch == null) return null;

        BranchResponse response = new BranchResponse();

        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setAddress(branch.getAddress());
        response.setIsActive(branch.getIsActive());
        response.setCreatedAt(branch.getCreatedAt());
        response.setUpdatedAt(branch.getUpdatedAt());

        if (branch.getPartners() != null) {

            PartnerSimpleDto partnerDto = new PartnerSimpleDto();

            partnerDto.setId(branch.getPartners().getId());
            partnerDto.setName(branch.getPartners().getName());

            response.setPartner(partnerDto);
        }

        response.setCreatedBy(mapUserToDto(branch.getCreatedBy()));
        response.setUpdatedBy(mapUserToDto(branch.getUpdatedBy()));

        return response;
    }

    private UserSimpleDto mapUserToDto(User user) {

        if (user == null) return null;

        UserSimpleDto dto = new UserSimpleDto();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        return dto;
    }
}
