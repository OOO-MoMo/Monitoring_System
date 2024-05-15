package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.response.JwtResponse;

public interface AuthService {

    JwtResponse register(UserCreateRequestDto registerRequest);

}
