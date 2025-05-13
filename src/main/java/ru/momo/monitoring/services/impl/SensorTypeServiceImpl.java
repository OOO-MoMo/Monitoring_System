package ru.momo.monitoring.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.SensorTypeService;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorTypeRequest;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.mapper.SensorTypeMapper;
import ru.momo.monitoring.store.repositories.SensorTypeRepository;

import java.util.UUID;

@Service
public class SensorTypeServiceImpl implements SensorTypeService {

    private final SensorTypeRepository sensorTypeRepository;
    private final SensorTypeMapper sensorTypeMapper;
    private SensorService sensorService;

    @Autowired
    public SensorTypeServiceImpl(SensorTypeRepository sensorTypeRepository, SensorTypeMapper sensorTypeMapper) {
        this.sensorTypeRepository = sensorTypeRepository;
        this.sensorTypeMapper = sensorTypeMapper;
    }

    @Autowired
    public void setSensorService(@Lazy SensorService sensorService) {
        this.sensorService = sensorService;
    }

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

    @Override
    @Transactional
    public SensorTypeDto updateSensorType(UUID id, UpdateSensorTypeRequest request) {
        sensorTypeRepository.throwIfExistsWithSameNameAndDifferentId(request.name(), id);

        SensorType existingSensorType = sensorTypeRepository.getByIdOrThrow(id);

        sensorTypeMapper.updateEntityFromRequest(request, existingSensorType);

        SensorType updatedSensorType = sensorTypeRepository.save(existingSensorType);

        return sensorTypeMapper.toDto(updatedSensorType);
    }

    @Transactional
    @Override
    public void deleteSensorType(UUID id) {
        if (!sensorTypeRepository.existsById(id)) {
            sensorTypeRepository.getByIdOrThrow(id);
        }

        if (sensorService.existsByTypeId(id)) {
            throw new SensorBadRequestException(
                    "Cannot delete SensorType with ID " + id + " because it is currently used by one or more Sensors."
            );
        }

        sensorTypeRepository.deleteById(id);
    }

}