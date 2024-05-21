package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.exceptions.user.SensorBadRequestException;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.store.dto.request.SensorCreateRequestDto;
import ru.momo.monitoring.store.dto.response.SensorCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.SensorResponseDto;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.repositories.SensorRepository;

import static ru.momo.monitoring.exceptions.user.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;

    @Override
    public SensorResponseDto getSensorById(Long id) {
        Sensor sensor = sensorRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("Technic with id = %d is not exist", id)
                );

        return SensorResponseDto.mapFromEntity(sensor);
    }

    @Override
    public SensorCreatedResponseDto create(SensorCreateRequestDto request) {
        if (sensorRepository.findByType(request.getType()).isPresent()) {
            throw new SensorBadRequestException("Sensor with type %s already exists", request.getType());
        }

        Sensor sensor = SensorCreateRequestDto.mapToEntity(request);
        sensorRepository.save(sensor);

        return SensorCreatedResponseDto.mapFromEntity(sensor);
    }

}
