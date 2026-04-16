package id.ac.ui.cs.advprog.mysawit.modules.auth.service;

import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.mysawit.modules.auth.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);
    AuthResponse currentSession(String email);
}
