package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.SensorTypeService;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.request.SensorAssignmentRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.dto.response.SensorsDto;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.mapper.SensorMapper;
import ru.momo.monitoring.store.repositories.SensorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;

    private final UserService userService;

    private final SensorTypeService sensorTypeService;

    private final SensorMapper sensorMapper;

    private final TechnicService technicService;

    @Override
    @Transactional
    public SensorDto registerSensor(CreateSensorRequest request, String email) {
        SensorType sensorType = sensorTypeService.getSensorTypeEntityById(request.sensorTypeId());

        User user = userService.getByEmail(email);

        sensorRepository.throwIfExistsWithSameSerialNumber(request.serialNumber());

        Sensor sensor = new Sensor();
        sensor.setType(sensorType);
        sensor.setSerialNumber(request.serialNumber());
        sensor.setManufacturer(request.manufacturer());
        sensor.setMinValue(request.minValue());
        sensor.setMaxValue(request.maxValue());
        sensor.setProductionDate(request.productionDate());
        sensor.setCompany(user.getCompany());

        sensorRepository.saveAndFlush(sensor);

        return SensorDto.toDto(sensor);
    }

    @Override
    @Transactional
    public void assignToTechnic(SensorAssignmentRequest request, String email) {
        Sensor sensor = sensorRepository.findByIdOrThrow(request.sensorId());

        User manager = userService.getByEmail(email);

        if (sensor.getCompany().getId() != manager.getCompany().getId()) {
            throw new SensorBadRequestException("Sensor assigned to another company");
        }

        Technic technic = technicService.findByCompanyAndId(manager.getCompany().getId(), request.technicId());

        if (technic.getSensors().contains(sensor)) {
            throw new EntityDuplicationException("Sensor already assigned to this technic");
        }

        technic.getSensors().add(sensor);
        sensor.setTechnic(technic);
        sensorRepository.saveAndFlush(sensor);
        technicService.save(technic);
    }


    @Override
    @Transactional
    public void unassignFromTechnic(SensorAssignmentRequest request, String email) {
        Sensor sensor = sensorRepository.findByIdOrThrow(request.sensorId());
        User manager = userService.getByEmail(email);

        if (!sensor.getCompany().getId().equals(manager.getCompany().getId())) {
            throw new SensorBadRequestException("Sensor belongs to another company");
        }

        Technic technic = technicService.findByCompanyAndId(
                manager.getCompany().getId(),
                request.technicId()
        );

        if (sensor.getTechnic() == null || !technic.equals(sensor.getTechnic())) {
            throw new SensorBadRequestException("Sensor is not assigned to this technic");
        }

        technic.getSensors().remove(sensor);
        sensor.setTechnic(null);

        technicService.save(technic);
        sensorRepository.saveAndFlush(sensor);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorsDto getAllCompanySensors(String email) {
        User manager = userService.getByEmail(email);

        List<Sensor> sensors = sensorRepository.findAllByCompanyId(manager.getCompany().getId());

        return new SensorsDto(
                sensors.stream().map(SensorDto::toDto).toList()
        );
    }

/*    private final SensorRepository sensorRepository;
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
    public SensorToTechnicResponseDto actionSensorToTechnic(SensorToTechnicRequestDto request) {
        Sensor sensor = getSensor(request.getSensorId());

        Technic technic = technicRepository
                .findById(request.getTechnicId())
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("Technic with id = %d is not exist", request.getTechnicId())
                );

        boolean isSensorExist = technic
                .getSensors()
                .stream()
                .anyMatch(e -> e.equals(sensor));

        if (request.getAction().equals(SensorToTechnicRequestDto.ATTACH)) {
            attachSensorToTechnic(sensor, technic, isSensorExist);
        } else {
            unpinSensorFromTechnic(sensor, technic, isSensorExist);
        }

        return new SensorToTechnicResponseDto(request);
    }

    private void unpinSensorFromTechnic(Sensor sensor, Technic technic, boolean isSensorExist) {
        if (!isSensorExist) {
            throw new SensorBadRequestException(
                    "Technic with id = %d have not sensor with id = %d ",
                    technic.getId(),
                    sensor.getSensorId()
            );
        }

        technic.getSensors().remove(sensor);

        technicRepository.save(technic);
    }

    private void attachSensorToTechnic(Sensor sensor, Technic technic, boolean isSensorExist) {
        if (isSensorExist) {
            throw new SensorBadRequestException(
                    "Technic with id = %d is already have sensor with id = %d ",
                    technic.getId(),
                    sensor.getSensorId()
            );
        }

        technic.getSensors().add(sensor);

        technicRepository.save(technic);
    }

    @Override
    public List<SensorResponseDto> getSensorByTechnicId(UUID technicId) {
        List<Sensor> response = null;

        if (technicRepository.existsById(technicId)) {
            response = sensorRepository.findByTechnicId(technicId);
        } return response != null
                ? response.stream().map(SensorResponseDto::mapFromEntity).toList()
                : null;
    }

    @Override
    public void delete(Long id) {
        Sensor deletedSensor = sensorRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier(
                                "Sensor with id = %d is not exist", id
                        )
                );
        sensorRepository.delete(deletedSensor);
    }

    private Sensor getSensor(Long id) {
        return sensorRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("Sensor with id = %d is not exist", id)
                );
    }*/

}
