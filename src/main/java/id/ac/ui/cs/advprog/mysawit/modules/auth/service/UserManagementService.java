package id.ac.ui.cs.advprog.mysawit.modules.auth.service;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.UserResponse;

import java.util.List;

public interface UserManagementService {

    /**
     * List all users, optionally filtered by role, name, or email substring.
     * Pass null to skip a filter.
     */
    List<UserResponse> listUsers(Role role, String name, String email);

    /** Get a single user by ID. Throws if not found. */
    UserResponse getUserById(Long userId);

    /**
     * Assign a Laborer to a Foreman.
     * Validates that laborerId refers to a LABORER and foremanId refers to a FOREMAN.
     */
    UserResponse assignForeman(Long laborerId, Long foremanId);

    /**
     * Remove a Laborer from their current Foreman assignment.
     * Throws if the Laborer is not currently assigned.
     */
    UserResponse unassignForeman(Long laborerId);

    /**
     * Delete a user account.
     * Throws IllegalStateException if requestingUserId == userId (admin cannot delete self).
     */
    void deleteUser(Long userId, Long requestingUserId);
}
