package id.ac.ui.cs.advprog.mysawit.modules.auth.service;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.core.model.User;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.UserResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;

    public UserManagementServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserResponse> listUsers(Role role, String name, String email) {
        Stream<User> stream = (role != null)
                ? userRepository.findByRole(role).stream()
                : userRepository.findAll().stream();

        if (name != null && !name.isBlank()) {
            String lowerName = name.toLowerCase();
            stream = stream.filter(u -> u.getUsername().toLowerCase().contains(lowerName));
        }

        if (email != null && !email.isBlank()) {
            String lowerEmail = email.toLowerCase();
            stream = stream.filter(u -> u.getEmail().toLowerCase().contains(lowerEmail));
        }

        return stream.map(this::toResponse).toList();
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = findUserOrThrow(userId);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse assignForeman(Long laborerId, Long foremanId) {
        User laborer = findUserOrThrow(laborerId);
        User foreman = findUserOrThrow(foremanId);

        if (laborer.getRole() != Role.LABORER) {
            throw new IllegalArgumentException("User " + laborerId + " is not a Laborer.");
        }
        if (foreman.getRole() != Role.FOREMAN) {
            throw new IllegalArgumentException("User " + foremanId + " is not a Foreman.");
        }

        laborer.setForemanId(foremanId);
        userRepository.save(laborer);

        return toResponse(laborer);
    }

    @Override
    @Transactional
    public UserResponse unassignForeman(Long laborerId) {
        User laborer = findUserOrThrow(laborerId);

        if (laborer.getRole() != Role.LABORER) {
            throw new IllegalArgumentException("User " + laborerId + " is not a Laborer.");
        }
        if (laborer.getForemanId() == null) {
            throw new IllegalStateException("Laborer " + laborerId + " is not assigned to any Foreman.");
        }

        laborer.setForemanId(null);
        userRepository.save(laborer);

        return toResponse(laborer);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Long requestingUserId) {
        if (userId.equals(requestingUserId)) {
            throw new IllegalStateException("Admin cannot delete their own account.");
        }

        User user = findUserOrThrow(userId);

        // If deleting a foreman, unassign all laborers currently under them
        if (user.getRole() == Role.FOREMAN) {
            List<User> assignedLaborers = userRepository.findByForemanId(userId);
            assignedLaborers.forEach(laborer -> laborer.setForemanId(null));
            userRepository.saveAll(assignedLaborers);
        }

        userRepository.deleteById(userId);
    }

    // ---- helpers ----

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getForemanId()
        );
    }
}
