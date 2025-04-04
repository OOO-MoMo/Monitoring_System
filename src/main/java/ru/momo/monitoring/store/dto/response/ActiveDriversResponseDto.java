package ru.momo.monitoring.store.dto.response;

import java.util.List;

public record ActiveDriversResponseDto(
        List<UserResponseDto> activeDrivers
) {
}
