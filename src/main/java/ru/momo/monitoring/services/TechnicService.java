package ru.momo.monitoring.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicDataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicUpdateResponseDto;

import java.util.UUID;

public interface TechnicService {

    TechnicResponseDto getTechById(UUID id);

    TechnicCreatedResponseDto create(TechnicCreateRequestDto request);

    TechnicUpdateResponseDto update(TechnicUpdateRequestDto request);

    void delete(UUID id);

    Page<TechnicResponseDto> getTechByUserId(UUID id, Pageable pageable, String brand, String model);

    TechnicDataResponseDto getSensorsData(UUID id);

}
