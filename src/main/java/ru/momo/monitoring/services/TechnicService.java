package ru.momo.monitoring.services;

import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;

public interface TechnicService {
    @Transactional(readOnly = true)
    TechnicResponseDto getTechById(Long id);

}
