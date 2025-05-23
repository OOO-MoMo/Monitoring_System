package ru.momo.monitoring.services;

import org.springframework.data.domain.Pageable;
import ru.momo.monitoring.store.dto.request.CompanyCreateRequestDto;
import ru.momo.monitoring.store.dto.request.CompanyUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.CompanyResponseDto;
import ru.momo.monitoring.store.dto.response.PageCompanyResponseDto;
import ru.momo.monitoring.store.entities.Company;

import java.util.List;
import java.util.UUID;

public interface CompanyService {

    CompanyResponseDto create(CompanyCreateRequestDto request);

    PageCompanyResponseDto findAll(String name, Pageable pageable);

    void delete(UUID id);

    CompanyResponseDto update(CompanyUpdateRequestDto request);

    CompanyResponseDto getCompanyById(UUID id);

    Company findById(UUID id);

    void save(Company company);

    Boolean isExistsById(UUID id);

    int countTotalCompanies();

    List<Company> findAllCompaniesForReport();

}
