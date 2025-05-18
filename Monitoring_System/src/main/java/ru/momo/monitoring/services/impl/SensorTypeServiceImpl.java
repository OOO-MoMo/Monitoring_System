package ru.momo.monitoring.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.SensorTypeService;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorTypeRequest;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.repositories.SensorTypeRepository;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SensorTypeServiceImpl implements SensorTypeService {

    private final SensorTypeRepository sensorTypeRepository;
    private SensorService sensorService;

    @Autowired
    public SensorTypeServiceImpl(SensorTypeRepository sensorTypeRepository) {
        this.sensorTypeRepository = sensorTypeRepository;
    }

    @Autowired
    public void setSensorService(@Lazy SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @Transactional
    @Override
    public SensorTypeDto createSensorType(CreateSensorTypeRequest request) {
        sensorTypeRepository.throwIfExistsWithSameName(request.name());

        SensorType sensorType = new SensorType();
        sensorType.setName(request.name());
        sensorType.setUnit(request.unit());
        sensorType.setDescription(request.description());
        if (request.metadata() != null) {
            sensorType.setMetadata(new HashMap<>(request.metadata()));
        } else {
            sensorType.setMetadata(new HashMap<>());
        }

        SensorType savedSensorType = sensorTypeRepository.save(sensorType);
        return SensorTypeDto.fromEntity(savedSensorType);
    }

    @Transactional(readOnly = true)
    @Override
    public SensorTypesDto getAllSensorTypes() {
        return new SensorTypesDto(
                sensorTypeRepository.findAll()
                        .stream()
                        .map(SensorTypeDto::fromEntity)
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    @Override
    public SensorTypeDto getSensorTypeById(UUID id) {
        SensorType sensorType = sensorTypeRepository.getByIdOrThrow(id);
        return SensorTypeDto.fromEntity(sensorType);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorType getSensorTypeEntityById(UUID id) {
        return sensorTypeRepository.getByIdOrThrow(id);
    }

    @Override
    @Transactional
    public SensorTypeDto updateSensorType(UUID id, UpdateSensorTypeRequest request) {
        sensorTypeRepository.throwIfExistsWithSameNameAndDifferentId(request.name(), id);

        SensorType existingSensorType = sensorTypeRepository.getByIdOrThrow(id);

        if (request.name() != null && !request.name().isBlank()) {
            existingSensorType.setName(request.name());
        }
        if (request.unit() != null && !request.unit().isBlank()) {
            existingSensorType.setUnit(request.unit());
        }

        existingSensorType.setDescription(request.description());

        if (request.metadata() != null) {
            existingSensorType.setMetadata(new HashMap<>(request.metadata()));
        }

        SensorType updatedSensorType = sensorTypeRepository.save(existingSensorType);
        return SensorTypeDto.fromEntity(updatedSensorType);
    }

    @Transactional
    @Override
    public void deleteSensorType(UUID id) {
        if (!sensorTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("SensorType with id " + id + " not found.");
        }

        if (sensorService.existsByTypeId(id)) {
            throw new SensorBadRequestException(
                    "Cannot delete SensorType with ID " + id + " because it is currently used by one or more Sensors."
            );
        }

        sensorTypeRepository.deleteById(id);
    }
}