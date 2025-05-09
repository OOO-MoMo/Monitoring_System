package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicPutDriverRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicDataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicPutDriverResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.entities.Technic;

import java.util.List;
import java.util.UUID;

public interface TechnicService {

    TechnicResponseDto getTechById(UUID id);

    TechnicCreatedResponseDto create(TechnicCreateRequestDto request);

    TechnicResponseDto update(UUID id, TechnicUpdateRequestDto request);

    void delete(UUID id);

    List<TechnicResponseDto> getFilteredTechnics(String email,
                                                 UUID ownerId,
                                                 Integer year,
                                                 String brand,
                                                 String model,
                                                 Boolean isActive);

    TechnicDataResponseDto getSensorsData(UUID id);

    TechnicPutDriverResponseDto putNewDriver(TechnicPutDriverRequestDto request);

    Technic findByCompanyAndId(UUID companyId, UUID id);

    void save(Technic technic);

    Technic getEntityById(UUID id);

    List<TechnicResponseDto> getAllTechnicsByCompanyId(UUID companyId);

    List<TechnicResponseDto> getTechnicsForDriver(String driverEmail);

    List<TechnicResponseDto> getAllTechnicsForManager();
}
