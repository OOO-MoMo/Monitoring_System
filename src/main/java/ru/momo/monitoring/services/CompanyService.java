package ru.momo.monitoring.services;

import org.springframework.data.domain.Pageable;
import ru.momo.monitoring.store.dto.request.CompanyCreateRequestDto;
import ru.momo.monitoring.store.dto.request.CompanyUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.CompanyResponseDto;
import ru.momo.monitoring.store.dto.response.PageCompanyResponseDto;
import ru.momo.monitoring.store.entities.Company;

import java.util.UUID;

public interface CompanyService {

    CompanyResponseDto create(CompanyCreateRequestDto request);

    PageCompanyResponseDto findAll(String name, Pageable pageable);

    void delete(UUID id);

    CompanyResponseDto update(CompanyUpdateRequestDto request);

    Company findById(UUID id);

    void save(Company company);

}
