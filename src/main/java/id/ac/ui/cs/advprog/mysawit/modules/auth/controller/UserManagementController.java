package id.ac.ui.cs.advprog.mysawit.modules.auth.controller;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AssignForemanRequest;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.UserResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.AuthService;
import id.ac.ui.cs.advprog.mysawit.modules.auth.service.UserManagementService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final AuthService authService;

    public UserManagementController(UserManagementService userManagementService,
                                    AuthService authService) {
        this.userManagementService = userManagementService;
        this.authService = authService;
    }

    // GET /api/users?role=LABORER&name=budi&email=budi@
    @GetMapping
    public ResponseEntity<List<UserResponse>> list(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            Authentication authentication
    ) {
        requireAdmin(authentication);

        Role roleFilter = null;
        if (role != null && !role.isBlank()) {
            try {
                roleFilter = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + role);
            }
        }

        return ResponseEntity.ok(userManagementService.listUsers(roleFilter, name, email));
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> detail(
            @PathVariable Long id,
            Authentication authentication
    ) {
        requireAdmin(authentication);
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    // PUT /api/users/{laborerId}/foreman
    @PutMapping("/{laborerId}/foreman")
    public ResponseEntity<UserResponse> assignForeman(
            @PathVariable Long laborerId,
            @RequestBody AssignForemanRequest request,
            Authentication authentication
    ) {
        requireAdmin(authentication);
        return ResponseEntity.ok(
                userManagementService.assignForeman(laborerId, request.getForemanId())
        );
    }

    // DELETE /api/users/{laborerId}/foreman
    @DeleteMapping("/{laborerId}/foreman")
    public ResponseEntity<UserResponse> unassignForeman(
            @PathVariable Long laborerId,
            Authentication authentication
    ) {
        requireAdmin(authentication);
        return ResponseEntity.ok(userManagementService.unassignForeman(laborerId));
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        requireAdmin(authentication);

        Long requestingUserId = resolveCurrentUserId(authentication);
        userManagementService.deleteUser(id, requestingUserId);

        return ResponseEntity.noContent().build();
    }

    // ---- exception handlers ----

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    // ---- helpers ----

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        AuthResponse session = authService.currentSession(authentication.getName());
        if (session.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Admin only");
        }
    }

    private Long resolveCurrentUserId(Authentication authentication) {
        // getUserById by email via authService, then look up their ID through the response
        // We use listUsers with email filter to get the ID — this is safe because email is unique
        String email = authentication.getName();
        List<UserResponse> matches = userManagementService.listUsers(null, null, email);
        return matches.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Could not resolve current user"))
                .getId();
    }
}
