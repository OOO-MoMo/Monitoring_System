package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.exceptions.user.SensorBadRequestException;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.store.dto.request.SensorCreateRequestDto;
import ru.momo.monitoring.store.dto.request.SensorToTechnicRequestDto;
import ru.momo.monitoring.store.dto.response.SensorCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.SensorResponseDto;
import ru.momo.monitoring.store.dto.response.SensorToTechnicResponseDto;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.repositories.SensorRepository;
import ru.momo.monitoring.store.repositories.TechnicRepository;

import java.util.List;

import static ru.momo.monitoring.exceptions.user.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final TechnicRepository technicRepository;

    @Override
    public SensorResponseDto getSensorById(Long id) {
        return SensorResponseDto.mapFromEntity(getSensor(id));
    }

    @Override
    public SensorCreatedResponseDto create(SensorCreateRequestDto request) {
        if (sensorRepository.findByType(request.getType().toLowerCase()).isPresent()) {
            throw new SensorBadRequestException("Sensor with type %s already exists", request.getType());
        }

        Sensor sensor = SensorCreateRequestDto.mapToEntity(request);
        sensorRepository.save(sensor);

        return SensorCreatedResponseDto.mapFromEntity(sensor);
    }

    @Override
    public SensorToTechnicResponseDto addSensorToTechnic(SensorToTechnicRequestDto request) {
        Sensor newSensor = getSensor(request.getSensorId());

        Technic technic = technicRepository
                .findById(request.getTechnicId())
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("Technic with id = %d is not exist", request.getTechnicId())
                );

        boolean isSensorExist = technic
                .getSensors()
                .stream()
                .anyMatch(sensor -> sensor.equals(newSensor));

        if (isSensorExist) {
            throw new SensorBadRequestException(
                    "Technic with id = %d is already have sensor with id = %d ",
                    request.getTechnicId(),
                    request.getSensorId()
            );
        }

        technic.getSensors().add(newSensor);

        technicRepository.save(technic);

        return new SensorToTechnicResponseDto(request);
    }

    @Override
    public List<SensorResponseDto> getSensorByTechnicId(Long technicId) {
        List<Sensor> response = null;

        if (technicRepository.existsById(technicId)) {
            response = sensorRepository.findByTechnicId(technicId);
        } return response != null
                ? response.stream().map(SensorResponseDto::mapFromEntity).toList()
                : null;
    }

    private Sensor getSensor(Long id) {
        return sensorRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("Sensor with id = %d is not exist", id)
                );
    }

}
