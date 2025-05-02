package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.services.SensorTypeService;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.mapper.SensorTypeMapper;
import ru.momo.monitoring.store.repositories.SensorTypeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SensorTypeServiceImpl implements SensorTypeService {

    private final SensorTypeRepository sensorTypeRepository;
    private final SensorTypeMapper sensorTypeMapper;

    @Transactional
    @Override
    public SensorTypeDto createSensorType(CreateSensorTypeRequest request) {
        sensorTypeRepository.throwIfExistsWithSameName(request.name());

        SensorType sensorType = sensorTypeMapper.toEntity(request);

        return sensorTypeMapper.toDto(sensorTypeRepository.save(sensorType));
    }

    @Transactional(readOnly = true)
    @Override
    public SensorTypesDto getAllSensorTypes() {
        return new SensorTypesDto(
                sensorTypeRepository.findAll()
                        .stream()
                        .map(sensorTypeMapper::toDto)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public SensorTypeDto getSensorTypeById(UUID id) {
        return sensorTypeMapper.toDto(sensorTypeRepository.getByIdOrThrow(id));
    }

    @Override
    public SensorType getSensorTypeEntityById(UUID id) {
        return sensorTypeRepository.getByIdOrThrow(id);
    }

}