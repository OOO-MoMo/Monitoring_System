package ru.momo.monitoring.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicDataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicUpdateResponseDto;

public interface TechnicService {

    @Transactional(readOnly = true)
    TechnicResponseDto getTechById(Long id);

    @Transactional(readOnly = false)
    TechnicCreatedResponseDto create(TechnicCreateRequestDto request);

    @Transactional(readOnly = false)
    TechnicUpdateResponseDto update(TechnicUpdateRequestDto request);

    @Transactional(readOnly = false)
    void delete(Long id);

    @Transactional(readOnly = true)
    Page<TechnicResponseDto> getTechByUserId(Long id, Pageable pageable, String brand, String model);

    @Transactional(readOnly = true)
    TechnicDataResponseDto getSensorsData(Long id);

}
