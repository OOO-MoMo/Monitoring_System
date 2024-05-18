package ru.momo.monitoring.services;

import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.*;

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
    TechnicResponseDto getTechByUserId(Long id);
}
