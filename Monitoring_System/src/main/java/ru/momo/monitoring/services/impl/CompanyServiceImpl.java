package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.services.CompanyService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.CompanyCreateRequestDto;
import ru.momo.monitoring.store.dto.request.CompanyUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.CompanyResponseDto;
import ru.momo.monitoring.store.dto.response.PageCompanyResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.repositories.CompanyRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserService userService;

    @Override
    @Transactional
    public CompanyResponseDto create(CompanyCreateRequestDto request) {
        validate(request);

        Company company = Company.builder()
                .inn(request.inn())
                .name(request.name())
                .headOfficeAddress(request.headOfficeAddress())
                .build();

        companyRepository.save(company);

        return CompanyResponseDto.mapFromEntity(company);
    }

    @Override
    @Transactional(readOnly = true)
    public PageCompanyResponseDto findAll(String name, Pageable pageable) {
        Page<Company> companies = companyRepository.findAllByNameContainingIgnoreCase(name != null ? name : "", pageable);

        List<CompanyResponseDto> content = companies.getContent().stream()
                .map(CompanyResponseDto::mapFromEntity)
                .toList();

        return new PageCompanyResponseDto(
                content,
                companies.getNumber(),
                companies.getSize(),
                companies.getTotalElements(),
                companies.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Company company = companyRepository.findByIdOrThrow(id);
        List<User> users = userService.findAllActiveByCompanyId(id);

        for (User user : users) {
            user.setIsActive(false);
            userService.save(user);
        }

        companyRepository.delete(company);
    }

    @Override
    @Transactional
    public CompanyResponseDto update(CompanyUpdateRequestDto request) {
        Company company = companyRepository.findByIdOrThrow(request.id());

        if (request.inn() != null) {
            companyRepository.throwIfExistWithSameInn(request.inn());
            company.setInn(request.inn());
        }

        if (request.name() != null) {
            companyRepository.throwIfExistWithSameName(request.name());
            company.setName(request.name());
        }

        if (request.headOfficeAddress() != null) {
            company.setHeadOfficeAddress(request.headOfficeAddress());
        }

        companyRepository.save(company);
        return CompanyResponseDto.mapFromEntity(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDto getCompanyById(UUID id) {
        Company company = companyRepository.findByIdOrThrow(id);
        
        return CompanyResponseDto.mapFromEntity(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Company findById(UUID id) {
        return companyRepository.findByIdOrThrow(id);
    }

    @Override
    public void save(Company company) {
        companyRepository.save(company);
    }

    @Override
    public Boolean isExistsById(UUID id) {
        return companyRepository.existsById(id);
    }

    private void validate(CompanyCreateRequestDto request) {
        companyRepository.throwIfExistWithSameInn(request.inn());
        companyRepository.throwIfExistWithSameName(request.name());
    }

}
