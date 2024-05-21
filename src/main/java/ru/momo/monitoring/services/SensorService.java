package ru.momo.monitoring.services;

import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.store.dto.request.SensorCreateRequestDto;
import ru.momo.monitoring.store.dto.response.SensorCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.SensorResponseDto;

public interface SensorService {

    @Transactional(readOnly = true)
    SensorResponseDto getSensorById(Long id);

    @Transactional(readOnly = false)
    SensorCreatedResponseDto create(SensorCreateRequestDto request);

}
