package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.services.SensorFactory;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.DataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicDataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicUpdateResponseDto;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.repositories.SensorRepository;
import ru.momo.monitoring.store.repositories.TechnicRepository;
import ru.momo.monitoring.store.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

import static ru.momo.monitoring.exceptions.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Service
@RequiredArgsConstructor
public class TechnicServiceImpl implements TechnicService {

    private final TechnicRepository technicRepository;

    private final UserRepository userRepository;

    private final SensorRepository sensorRepository;

    private final SensorFactory sensorFactory;


    @Override
    @Transactional(readOnly = true)
    public TechnicResponseDto getTechById(UUID id) {
        return TechnicResponseDto.mapFromEntity(getTechnic(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TechnicResponseDto> getTechByUserId(UUID userId,
                                                    Pageable pageable,
                                                    String brand,
                                                    String model) {
        if (userRepository.existsById(userId)) {
            Page<Technic> response = technicRepository.findAllByOwnerIdIdAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
                    userId,
                    brand,
                    model,
                    pageable
            );

            if (response.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Nothing was found on page number %d", pageable.getPageNumber()
                );
            }

            return response.map(TechnicResponseDto::mapFromEntity);
        } else {
            throw new ResourceNotFoundException("User with id = %d is not exist", userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicDataResponseDto getSensorsData(UUID id) {
        Technic technic = getTechnic(id);
        List<Sensor> technicSensors = sensorRepository.findByTechnicId(id);

        if (technicRepository == null) {
            throw new ResourceNotFoundException("Technic with id = %d is not have any sensors", id);
        }

        List<DataResponseDto> data = technicSensors
                .stream()
                .map(e -> sensorFactory.getSensor(e.getType()))
                .map(DataResponseDto::mapFromEntity)
                .toList();

        return new TechnicDataResponseDto(technic.getModel(), technic.getBrand(), data);
    }

    @Override
    @Transactional
    public TechnicCreatedResponseDto create(TechnicCreateRequestDto request) {
        Technic technic = TechnicCreateRequestDto.mapToTechnicEntity(request);
        technicRepository.save(technic);
        return TechnicCreatedResponseDto.MapFromEntity(technic);
    }

    @Override
    @Transactional
    public TechnicUpdateResponseDto update(TechnicUpdateRequestDto request) {
        Technic updatedTechnic = getTechnic(request.getTechnicId());

        if (request.getNewBrand() != null) {
            updatedTechnic.setBrand(request.getNewBrand());
        }
        if (request.getNewModel() != null) {
            updatedTechnic.setModel(request.getNewModel());
        }

        technicRepository.save(updatedTechnic);

        return TechnicUpdateResponseDto.mapFromEntity(updatedTechnic);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Technic deletedTechnic = getTechnic(id);
        technicRepository.delete(deletedTechnic);
    }

    private Technic getTechnic(UUID id) {
        return technicRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("Technic with id = %d is not exist", id)
                );
    }

}
