package ru.momo.monitoring.store.dto.response;

import java.util.List;

public record UsersResponseDto(
        List<UserResponseDto> activeDrivers
) {
}
