package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.JwtRequest;
import ru.momo.monitoring.store.dto.request.RefreshJwtRequest;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.JwtResponse;
import ru.momo.monitoring.store.dto.response.RegisterJwtResponse;

public interface AuthService {

    JwtResponse login(JwtRequest loginRequest);

    JwtResponse refresh(RefreshJwtRequest refreshToken);

    RegisterJwtResponse register(RegisterRequest request, String username);

    String confirm(String token);

    String resendConfirmationEmail(String email);
}
