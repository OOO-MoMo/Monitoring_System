package ru.momo.monitoring.services;

import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.store.dto.request.SensorCreateRequestDto;
import ru.momo.monitoring.store.dto.request.SensorToTechnicRequestDto;
import ru.momo.monitoring.store.dto.response.SensorCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.SensorResponseDto;
import ru.momo.monitoring.store.dto.response.SensorToTechnicResponseDto;

import java.util.List;
import java.util.UUID;

public interface SensorService {

    @Transactional(readOnly = true)
    SensorResponseDto getSensorById(Long id);

    @Transactional(readOnly = false)
    SensorCreatedResponseDto create(SensorCreateRequestDto request);

    @Transactional(readOnly = false)
    SensorToTechnicResponseDto actionSensorToTechnic(SensorToTechnicRequestDto request);

    @Transactional(readOnly = true)
    List<SensorResponseDto> getSensorByTechnicId(UUID technicId);

    void delete(Long id);
}
