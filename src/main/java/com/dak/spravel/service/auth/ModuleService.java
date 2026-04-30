package com.dak.spravel.service.auth;

import com.dak.spravel.dto.request.user.CreateModuleRequest;
import com.dak.spravel.dto.request.user.UpdateModuleRequest;
import com.dak.spravel.dto.response.ModuleResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.Module;
import com.dak.spravel.repository.auth.ModuleRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Business logic for modules with slug immutability enforcement.
 */
@Service
@RequiredArgsConstructor
public class ModuleService {
    private final ModuleRepository moduleRepository;

    public List<ModuleResponse> findAll() {
        return moduleRepository.findAll().stream()
                .sorted(Comparator.comparing(Module::getSlug))
                .map(this::toResponse)
                .toList();
    }

    public ModuleResponse findById(Long id) {
        return toResponse(moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id)));
    }

    public ModuleResponse create(CreateModuleRequest request) {
        if (moduleRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Module slug '" + request.getSlug() + "' already exists");
        }
        Module m = new Module();
        m.setSlug(request.getSlug());
        m.setName(request.getName());
        m.setDescription(request.getDescription());
        return toResponse(moduleRepository.save(m));
    }

    public ModuleResponse update(Long id, UpdateModuleRequest request) {
        Module m = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));
        if (request.getName() != null && !request.getName().isBlank()) {
            m.setName(request.getName());
        }
        if (request.getDescription() != null) {
            m.setDescription(request.getDescription());
        }
        return toResponse(moduleRepository.save(m));
    }

    public void delete(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module", id);
        }
        moduleRepository.deleteById(id);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    public ModuleResponse toResponse(Module m) {
        ModuleResponse res = new ModuleResponse();
        res.setId(m.getId());
        res.setSlug(m.getSlug());
        res.setName(m.getName());
        res.setDescription(m.getDescription());
        return res;
    }
}
