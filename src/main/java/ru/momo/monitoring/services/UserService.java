package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.UserCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserUpdateResponseDto;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;

import java.util.UUID;

public interface UserService {

    UserResponseDto getById(UUID id);

    User getByIdEntity(UUID id);

    User getByEmail(String email);

    UserCreatedResponseDto create(UserCreateRequestDto request);

    UserUpdateResponseDto update(UserUpdateRequestDto request);

    void delete(UUID id);

    User saveNotConfirmedUser(RegisterRequest request, RoleName role);

    User confirmUser(String email);

    RoleName getNewUserRoleByCurrentUser(String username);

}
