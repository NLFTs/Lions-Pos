package com.fts.twin.service.auth;

import com.fts.twin.dto.request.user.CreatePermissionRequest;
import com.fts.twin.dto.request.user.UpdatePermissionRequest;
import com.fts.twin.dto.response.PermissionResponse;
import com.fts.twin.handler.ResourceNotFoundException;
import com.fts.twin.model.auth.Module;
import com.fts.twin.model.auth.Permission;
import com.fts.twin.repository.auth.ModuleRepository;
import com.fts.twin.repository.auth.PermissionRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Business logic for permissions with module grouping support.
 */
@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final ModuleRepository moduleRepository;

    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing((Permission p) -> p.getModule().getSlug())
                        .thenComparing(Permission::getSlug))
                .map(this::toResponse)
                .toList();
    }

    public Map<String, List<PermissionResponse>> findAllGrouped() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing((Permission p) -> p.getModule().getSlug())
                        .thenComparing(Permission::getSlug))
                .map(this::toResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModuleSlug));
    }

    public PermissionResponse findById(Long id) {
        return toResponse(permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id)));
    }

    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Permission slug '" + request.getSlug() + "' already exists");
        }
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module", request.getModuleId()));
        Permission p = new Permission();
        p.setSlug(request.getSlug());
        p.setName(request.getName());
        p.setModule(module);
        return toResponse(permissionRepository.save(p));
    }

    public PermissionResponse update(Long id, UpdatePermissionRequest request) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id));
        if (request.getName() != null && !request.getName().isBlank()) {
            p.setName(request.getName());
        }
        if (request.getModuleId() != null) {
            Module module = moduleRepository.findById(request.getModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Module", request.getModuleId()));
            p.setModule(module);
        }
        return toResponse(permissionRepository.save(p));
    }

    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission", id);
        }
        permissionRepository.deleteById(id);
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────────────────

    public PermissionResponse toResponse(Permission p) {
        PermissionResponse res = new PermissionResponse();
        res.setId(p.getId());
        res.setSlug(p.getSlug());
        res.setName(p.getName());
        res.setModuleSlug(p.getModule().getSlug());
        return res;
    }
}
