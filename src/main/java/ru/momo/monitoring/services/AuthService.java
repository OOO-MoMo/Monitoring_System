package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.JwtRequest;
import ru.momo.monitoring.store.dto.request.RefreshJwtRequest;
import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.response.JwtResponse;

public interface AuthService {

    JwtResponse login(JwtRequest loginRequest);

    JwtResponse refresh(RefreshJwtRequest refreshToken);

    JwtResponse register(UserCreateRequestDto registerRequest);

}
