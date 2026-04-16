package id.ac.ui.cs.advprog.mysawit.modules.auth.dto;

import id.ac.ui.cs.advprog.mysawit.core.model.Role;
import lombok.Getter;

@Getter
public class AuthResponse {
    private final String message;
    private final String username;
    private final Role role;

    public AuthResponse(String message, String username, Role role) {
        this.message = message;
        this.username = username;
        this.role = role;
    }
}