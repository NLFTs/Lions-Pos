package com.dak.spravel.controller.auth;

import com.dak.spravel.dto.request.user.ChangePasswordRequest;
import com.dak.spravel.dto.request.user.CreateUserRequest;
import com.dak.spravel.dto.request.user.UpdateUserRequest;
import com.dak.spravel.dto.response.ResData;
import com.dak.spravel.dto.response.UserResponse;
import com.dak.spravel.handler.ResourceNotFoundException;
import com.dak.spravel.model.auth.User;
import com.dak.spravel.repository.auth.UserRepository;
import com.dak.spravel.service.auth.UserService;
import com.dak.spravel.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Provides full CRUD operations for users with pagination, search,
 * and password change functionality.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/users
     * List all users.
     */
    @GetMapping
    public ResponseEntity<ResData<List<UserResponse>>> index() {
        log.info("[GET] /api/v1/users");
        return ResponseBuilder.ok(userService.findAll());
    }

    @GetMapping("/page")
    public ResponseEntity<ResData<Page<UserResponse>>> paginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        log.info("[GET] /api/v1/users/page page={} size={} search={}", page, size, search);
        return ResponseBuilder.ok(userService.findAll(page, size, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResData<UserResponse>> show(@PathVariable Long id) {
        log.info("[GET] /api/v1/users/{}", id);
        return ResponseBuilder.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResData<UserResponse>> store(
            @Valid @RequestBody CreateUserRequest request) {
        log.info("[POST] /api/v1/users username={}", request.getUsername());
        return ResponseBuilder.created(userService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResData<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("[PUT] /api/v1/users/{}", id);
        return ResponseBuilder.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResData<Void>> destroy(@PathVariable Long id) {
        log.info("[DELETE] /api/v1/users/{}", id);
        userService.delete(id);
        return ResponseBuilder.ok();
    }

    /**
     * PUT /api/v1/users/me/password
     * Update the password of the current authenticated user.
     */
    @PutMapping("/me/password")
    public ResponseEntity<ResData<Void>> changeMyPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        log.info("[PUT] /api/v1/users/me/password userId={}", user.getId());
        userService.changePassword(user.getId(), request);
        return ResponseBuilder.ok();
    }
}
