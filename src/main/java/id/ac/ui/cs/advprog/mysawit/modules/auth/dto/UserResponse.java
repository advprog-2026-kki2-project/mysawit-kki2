package id.ac.ui.cs.advprog.mysawit.modules.auth.dto;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private Role role;
    /** ID of the assigned Foreman; null if not assigned. Only applicable for LABORER role. */
    private Long foremanId;
}
