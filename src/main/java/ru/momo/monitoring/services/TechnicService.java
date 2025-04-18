package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicPutDriverRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicDataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicPutDriverResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicUpdateResponseDto;

import java.util.List;
import java.util.UUID;

public interface TechnicService {

    TechnicResponseDto getTechById(UUID id);

    TechnicCreatedResponseDto create(TechnicCreateRequestDto request);

    TechnicUpdateResponseDto update(TechnicUpdateRequestDto request);

    void delete(UUID id);

    List<TechnicResponseDto> getFilteredTechnics(UUID companyId,
                                                 UUID ownerId,
                                                 Integer year,
                                                 String brand,
                                                 String model,
                                                 Boolean isActive);

    TechnicDataResponseDto getSensorsData(UUID id);

    TechnicPutDriverResponseDto putNewDriver(TechnicPutDriverRequestDto request);

}
