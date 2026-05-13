package com.dak.spravel.service.auth;

import com.dak.spravel.dto.request.user.ChangePasswordRequest;
import com.dak.spravel.dto.request.user.CreateUserRequest;
import com.dak.spravel.dto.request.user.UpdateUserRequest;
import com.dak.spravel.dto.response.UserResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Role;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.RoleRepository;
import com.dak.spravel.repository.auth.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService permissionCacheService;

    // --- HELPER ---

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
                .anyMatch(role -> role.getSlug().equals("admin"));
    }

    private boolean isAdminPartner(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getSlug().equals("admin-partners"));
    }

    // --- LOGIC UTAMA ---

    // GET ALL
    public List<UserResponse> findAll() {
        User currentUser = getAuthenticatedUser();

        // Super admin bisa lihat semua user
        if (isAdmin(currentUser)) {
            return userRepository.findAll(Sort.by("username").ascending())
                    .stream().map(this::toResponse).toList();
        }

        // Admin partner hanya lihat user partnernya sendiri
        if (isAdminPartner(currentUser) && currentUser.getPartner() != null) {
            return userRepository.findByPartnerId(currentUser.getPartner().getId())
                    .stream().map(this::toResponse).toList();
        }

        throw new RuntimeException("Akses Ditolak: Anda tidak punya akses ke data user.");
    }

    // GET ALL PAGINATED
    public Page<UserResponse> findAll(int page, int size, String search) {
        User currentUser = getAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        if (isAdmin(currentUser)) {
            if (search != null && !search.isBlank()) {
                return userRepository.search(search, pageable).map(this::toResponse);
            }
            return userRepository.findAll(pageable).map(this::toResponse);
        }

        if (isAdminPartner(currentUser) && currentUser.getPartner() != null) {
            return userRepository.findByPartnerId(currentUser.getPartner().getId(), pageable)
                    .map(this::toResponse);
        }

        throw new RuntimeException("Akses Ditolak: Anda tidak punya akses ke data user.");
    }

    // GET BY ID
    public UserResponse findById(Long id) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Super admin bisa lihat semua
        if (isAdmin(currentUser)) {
            return toResponse(user);
        }

        // Admin partner hanya bisa lihat user partnernya sendiri
        if (isAdminPartner(currentUser) && currentUser.getPartner() != null) {
            if (user.getPartner() == null ||
                    !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
            return toResponse(user);
        }

        throw new RuntimeException("Akses Ditolak: Anda tidak punya akses ke data user.");
    }

    // CREATE
    public UserResponse create(CreateUserRequest request) {
        User currentUser = getAuthenticatedUser();

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Admin partner hanya bisa create user untuk partnernya sendiri
        if (isAdminPartner(currentUser)) {
            if (currentUser.getPartner() == null) {
                throw new RuntimeException("User ini tidak terasosiasi dengan Partner manapun.");
            }
            user.setPartner(currentUser.getPartner());

            // Admin partner hanya bisa assign role employee-partners
            Set<Role> roles = new HashSet<>();
            Role employeeRole = roleRepository.findBySlug("employee-partners")
                    .orElseThrow(() -> new RuntimeException("Role employee-partners tidak ditemukan"));
            roles.add(employeeRole);
            user.setRoles(roles);
        } else if (isAdmin(currentUser)) {
            // Super admin bisa assign role apapun
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                user.setRoles(resolveRoles(request.getRoleIds()));
            }
        } else {
            throw new RuntimeException("Akses Ditolak: Anda tidak punya akses untuk membuat user.");
        }

        return toResponse(userRepository.save(user));
    }

    // UPDATE
    public UserResponse update(Long id, UpdateUserRequest request) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Admin partner hanya bisa update user partnernya sendiri
        if (isAdminPartner(currentUser)) {
            if (user.getPartner() == null ||
                    !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        } else if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak punya akses untuk mengubah user.");
        }

        if (request.getUsername() != null) {
            if (!request.getUsername().equals(user.getUsername()) &&
                    userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getFullname() != null) user.setFullname(request.getFullname());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Hanya super admin yang bisa ganti role
        if (isAdmin(currentUser) && request.getRoleIds() != null) {
            Set<Role> roles = resolveRoles(request.getRoleIds());
            user.setRoles(roles);
            permissionCacheService.evict(user.getUsername());
        }

        return toResponse(userRepository.save(user));
    }

    // DELETE
    public void delete(Long id) {
        User currentUser = getAuthenticatedUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Admin partner hanya bisa delete user partnernya sendiri
        if (isAdminPartner(currentUser)) {
            if (user.getPartner() == null ||
                    !user.getPartner().getId().equals(currentUser.getPartner().getId())) {
                throw new RuntimeException("Akses Ditolak: User bukan bagian dari partner Anda.");
            }
        } else if (!isAdmin(currentUser)) {
            throw new RuntimeException("Akses Ditolak: Anda tidak punya akses untuk menghapus user.");
        }

        permissionCacheService.evict(user.getUsername());
        userRepository.deleteById(id);
    }

    // CHANGE PASSWORD
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // MAPPING
    private UserResponse toResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setFullname(user.getFullname());
        res.setEmail(user.getEmail());
        res.setCreatedAt(user.getCreatedAt());

        List<UserResponse.RoleData> roleDataList = user.getRoles().stream().map(role -> {
            UserResponse.RoleData rd = new UserResponse.RoleData();
            rd.setId(role.getId());
            rd.setSlug(role.getSlug());
            rd.setName(role.getName());
            return rd;
        }).toList();
        res.setRoles(roleDataList);

        return res;
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
            roles.add(role);
        }
        return roles;
    }
}