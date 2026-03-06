package id.ac.ui.cs.advprog.mysawit.modules.auth.dto;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private Role role;
}