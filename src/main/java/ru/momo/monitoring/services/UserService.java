package ru.momo.monitoring.services;

import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.store.dto.UserResponseDto;

public interface UserService {

    @Transactional(readOnly = true)
    UserResponseDto getById(Long id);

}
