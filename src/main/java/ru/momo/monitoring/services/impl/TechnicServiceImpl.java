package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.repositories.TechnicRepository;

import static ru.momo.monitoring.exceptions.user.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Service
@RequiredArgsConstructor
public class TechnicServiceImpl implements TechnicService {
    private final TechnicRepository technicRepository;
    @Override
    public TechnicResponseDto getTechById(Long id) {
        Technic technic = technicRepository
                .findById(id)
                .orElseThrow(
                    resourceNotFoundExceptionSupplier("КУ ПРИВЕТ ГОЙДА!!!")
                );

        return TechnicResponseDto.mapFromEntity(technic);
    }
}
