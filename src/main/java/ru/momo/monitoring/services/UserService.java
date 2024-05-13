package ru.momo.monitoring.services;

import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.UserCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserUpdateResponseDto;

public interface UserService {

    @Transactional(readOnly = true)
    UserResponseDto getById(Long id);

    @Transactional(readOnly = false)
    UserCreatedResponseDto create(UserCreateRequestDto request);

    @Transactional(readOnly = false)
    UserUpdateResponseDto update(UserUpdateRequestDto request);

}
