package id.ac.ui.cs.advprog.mysawit.modules.auth.service;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import id.ac.ui.cs.advprog.mysawit.core.model.User;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.UserResponse;
import id.ac.ui.cs.advprog.mysawit.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserManagementServiceImpl service;

    private User laborer;
    private User foreman;
    private User admin;

    @BeforeEach
    void setUp() {
        laborer = new User();
        laborer.setId(1L);
        laborer.setEmail("laborer@test.com");
        laborer.setUsername("laborer1");
        laborer.setRole(Role.LABORER);
        laborer.setPassword("hashed");

        foreman = new User();
        foreman.setId(2L);
        foreman.setEmail("foreman@test.com");
        foreman.setUsername("foreman1");
        foreman.setRole(Role.FOREMAN);
        foreman.setPassword("hashed");

        admin = new User();
        admin.setId(3L);
        admin.setEmail("admin@test.com");
        admin.setUsername("admin1");
        admin.setRole(Role.ADMIN);
        admin.setPassword("hashed");
    }

    // --- listUsers ---

    @Test
    void listUsers_returnsAllUsersWhenNoFilter() {
        when(userRepository.findAll()).thenReturn(List.of(laborer, foreman, admin));

        List<UserResponse> result = service.listUsers(null, null, null);

        assertThat(result).hasSize(3);
    }

    @Test
    void listUsers_filtersByRole() {
        when(userRepository.findByRole(Role.LABORER)).thenReturn(List.of(laborer));

        List<UserResponse> result = service.listUsers(Role.LABORER, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.LABORER);
    }

    @Test
    void listUsers_filtersByNameSubstring() {
        when(userRepository.findAll()).thenReturn(List.of(laborer, foreman));

        List<UserResponse> result = service.listUsers(null, "laborer", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("laborer1");
    }

    // --- getUserById ---

    @Test
    void getUserById_returnsUserWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(laborer));

        UserResponse response = service.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo(Role.LABORER);
    }

    @Test
    void getUserById_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // --- assignForeman ---

    @Test
    void assignForeman_setsForeman_whenBothRolesCorrect() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(laborer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(foreman));
        when(userRepository.save(any())).thenReturn(laborer);

        UserResponse result = service.assignForeman(1L, 2L);

        assertThat(laborer.getForemanId()).isEqualTo(2L);
        assertThat(result.getForemanId()).isEqualTo(2L);
        verify(userRepository).save(laborer);
    }

    @Test
    void assignForeman_throwsWhenTargetIsNotLaborer() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(foreman));  // foreman is not laborer
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.assignForeman(2L, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a Laborer");
    }

    @Test
    void assignForeman_throwsWhenForemanIdRefersToNonForeman() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(laborer));
        when(userRepository.findById(3L)).thenReturn(Optional.of(admin)); // admin is not foreman

        assertThatThrownBy(() -> service.assignForeman(1L, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a Foreman");
    }

    // --- unassignForeman ---

    @Test
    void unassignForeman_clearsAssignment() {
        laborer.setForemanId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(laborer));
        when(userRepository.save(any())).thenReturn(laborer);

        UserResponse result = service.unassignForeman(1L);

        assertThat(laborer.getForemanId()).isNull();
        assertThat(result.getForemanId()).isNull();
    }

    @Test
    void unassignForeman_throwsWhenNotAssigned() {
        laborer.setForemanId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(laborer));

        assertThatThrownBy(() -> service.unassignForeman(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not assigned");
    }

    @Test
    void unassignForeman_throwsWhenNotLaborer() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(foreman)); // foreman can't be unassigned

        assertThatThrownBy(() -> service.unassignForeman(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a Laborer");
    }

    // --- deleteUser ---

    @Test
    void deleteUser_deletesSuccessfully() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(foreman));
        when(userRepository.findByForemanId(2L)).thenReturn(List.of());

        service.deleteUser(2L, 3L);

        verify(userRepository).deleteById(2L);
    }

    @Test
    void deleteUser_throwsWhenDeletingSelf() {
        assertThatThrownBy(() -> service.deleteUser(3L, 3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot delete their own account");
    }

    @Test
    void deleteUser_unassignsLaborersWhenDeletingForeman() {
        laborer.setForemanId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(foreman));
        when(userRepository.findByForemanId(2L)).thenReturn(List.of(laborer));
        when(userRepository.saveAll(any())).thenReturn(List.of(laborer));

        service.deleteUser(2L, 3L);

        assertThat(laborer.getForemanId()).isNull();
        verify(userRepository).saveAll(List.of(laborer));
        verify(userRepository).deleteById(2L);
    }
}
