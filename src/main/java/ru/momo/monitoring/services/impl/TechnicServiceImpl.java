package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.repositories.TechnicRepository;

@Service
@RequiredArgsConstructor
public class TechnicServiceImpl implements TechnicService {
    @Override
    public TechnicResponseDto getTechById(Long id) {
        Technic technic = TechnicRepository
                .findById(id)
                .orElseThrow(

                );

        return TechnicResponseDto.mapFromEntity(technic);
    }
}
