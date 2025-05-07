package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.services.CompanyService;
import ru.momo.monitoring.services.SecurityService;
import ru.momo.monitoring.services.SensorFactory;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicPutDriverRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicDataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicPutDriverResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.repositories.SensorRepository;
import ru.momo.monitoring.store.repositories.TechnicRepository;
import ru.momo.monitoring.store.repositories.specification.TechnicSpecification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicServiceImpl implements TechnicService {

    private final TechnicRepository technicRepository;

    private final SensorRepository sensorRepository;

    private final SensorFactory sensorFactory;

    private final UserService userService;

    private final CompanyService companyService;

    private final SecurityService securityService;

    @Override
    @Transactional(readOnly = true)
    public TechnicResponseDto getTechById(UUID id) {
        return TechnicResponseDto.mapFromEntity(technicRepository.findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public TechnicCreatedResponseDto create(TechnicCreateRequestDto request) {
        Technic technic = TechnicCreateRequestDto.mapToTechnicEntity(request);
        Company company = companyService.findById(request.getCompanyId());

        validateNewTechnic(request);

        technic.setCompany(company);
        company.addTechnic(technic);

        technicRepository.save(technic);
        companyService.save(company);

        return TechnicCreatedResponseDto.MapFromEntity(technic);
    }

    @Override
    public List<TechnicResponseDto> getFilteredTechnics(String email,
                                                        UUID ownerId,
                                                        Integer year,
                                                        String brand,
                                                        String model,
                                                        Boolean isActive) {
        User manager = userService.getByEmail(email);

        Specification<Technic> spec = TechnicSpecification.filterTechnics(
                manager.getCompany().getId(), ownerId, year, brand, model, isActive
        );

        List<Technic> technics = technicRepository.findAll(spec);

        List<TechnicResponseDto> responseDtos = new ArrayList<>();

        for (Technic technic : technics) {
            responseDtos.add(TechnicResponseDto.mapFromEntity(technic));
        }

        return responseDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicDataResponseDto getSensorsData(UUID id) {
/*        Technic technic = technicRepository.findByIdOrThrow(id);
        List<Sensor> technicSensors = sensorRepository.findByTechnicId(id);

        List<DataResponseDto> data = technicSensors
                .stream()
                .map(e -> sensorFactory.getSensor(e.getType()))
                .map(DataResponseDto::mapFromEntity)
                .toList();

        return new TechnicDataResponseDto(technic.getModel(), technic.getBrand(), data);*/

        throw new NotImplementedException();
    }

    @Override
    public TechnicPutDriverResponseDto putNewDriver(TechnicPutDriverRequestDto request) {
        Technic technic = technicRepository.findByIdOrThrow(request.technicId());
        User user = userService.getByIdEntity(request.driverId());

        technic.setOwnerId(user);
        user.addTechnic(technic);
        technicRepository.save(technic);

        return new TechnicPutDriverResponseDto(request.technicId(), request.driverId());
    }

    @Override
    public Technic findByCompanyAndId(UUID companyId, UUID id) {
        Technic technic = technicRepository.findByIdOrThrow(id);

        if (!technic.getCompany().getId().equals(companyId)) {
            throw new SensorBadRequestException(
                    "Invalid company id. Technic id: " + technic.getId() + " is not in this company."
            );
        }

        return technic;
    }

    @Override
    public void save(Technic technic) {
        technicRepository.save(technic);
    }

    @Override
    @Transactional
    public TechnicResponseDto update(UUID id, TechnicUpdateRequestDto request) {
        Technic technicToUpdate = technicRepository.findByIdOrThrow(id);

        User currentUser = securityService.getCurrentUser();
        if (currentUser.getRole().equals(RoleName.ROLE_MANAGER) &&
                !technicToUpdate.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new AccessDeniedException("Manager can only update technics within their own company.");
        }

        updateFieldIfNotNull(request.brand(), technicToUpdate::setBrand);
        updateFieldIfNotNull(request.model(), technicToUpdate::setModel);
        updateFieldIfNotNull(request.year(), technicToUpdate::setYear);
        updateFieldIfNotNull(request.isActive(), technicToUpdate::setIsActive);
        updateFieldIfNotNull(request.description(), technicToUpdate::setDescription);
        updateFieldIfNotNull(request.lastServiceDate(), technicToUpdate::setLastServiceDate);
        updateFieldIfNotNull(request.nextServiceDate(), technicToUpdate::setNextServiceDate);

        Technic updatedTechnic = technicRepository.save(technicToUpdate);

        return TechnicResponseDto.mapFromEntity(updatedTechnic);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Technic deletedTechnic = technicRepository.findByIdOrThrow(id);

        User currentUser = securityService.getCurrentUser();
        if (currentUser.getRole().equals(RoleName.ROLE_MANAGER) &&
                !deletedTechnic.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new AccessDeniedException("Manager can only delete technics within their own company.");
        }

        technicRepository.delete(deletedTechnic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicResponseDto> getAllTechnicsByCompanyId(UUID companyId) {
        companyService.findById(companyId);

        List<Technic> technics = technicRepository.findByCompanyId(companyId);

        return technics.stream()
                .map(TechnicResponseDto::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicResponseDto> getTechnicsForDriver(String driverEmail) {
        User driver = userService.getByEmail(driverEmail);

        List<Technic> driverTechnics = driver.getTechnics();

        if (driverTechnics == null) {
            return Collections.emptyList();
        }

        return driverTechnics.stream()
                .map(TechnicResponseDto::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicResponseDto> getAllTechnicsForManager() {
        User currentUser = securityService.getCurrentUser();

        UUID companyId = currentUser.getCompany().getId();

        List<Technic> technics = technicRepository.findByCompanyId(companyId);

        return technics.stream().map(TechnicResponseDto::mapFromEntity).toList();
    }

    private <T> void updateFieldIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private void validateNewTechnic(TechnicCreateRequestDto request) {
        technicRepository.throwIfExistWithSameSerialNumber(request.getSerialNumber());
        technicRepository.throwIfExistWithSameVin(request.getVin());
    }

}
