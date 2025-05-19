package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.client.SensorGeneratorClient;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.services.CompanyService;
import ru.momo.monitoring.services.SecurityService;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.SensorTypeService;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.data_generator.RegisterSensorToGeneratorRequest;
import ru.momo.monitoring.store.dto.report.SensorValueStatsDto;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.request.SensorAssignmentRequest;
import ru.momo.monitoring.store.dto.request.SensorDataHistoryDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.dto.response.SensorsDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.SensorData;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.AggregationType;
import ru.momo.monitoring.store.entities.enums.DataGranularity;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.entities.enums.SensorStatus;
import ru.momo.monitoring.store.projection.AggregatedSensorDataViewImpl;
import ru.momo.monitoring.store.repositories.SensorDataRepository;
import ru.momo.monitoring.store.repositories.SensorRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;

    private final UserService userService;

    private final SensorTypeService sensorTypeService;

    private final TechnicService technicService;

    private final CompanyService companyService;

    private final SecurityService securityService;

    private final SensorGeneratorClient sensorGeneratorClient;

    private final SensorDataRepository sensorDataRepository;

    @Override
    @Transactional
    public SensorDto registerSensor(CreateSensorRequest request) {
        SensorType sensorType = sensorTypeService.getSensorTypeEntityById(request.sensorTypeId());
        Company sensorCompany = companyService.findById(request.companyId());

        sensorRepository.throwIfExistsWithSameSerialNumber(request.serialNumber());

        Sensor sensor = new Sensor();
        sensor.setType(sensorType);
        sensor.setSerialNumber(request.serialNumber());
        sensor.setManufacturer(request.manufacturer());
        sensor.setMinValue(request.minValue());
        sensor.setMaxValue(request.maxValue());
        sensor.setProductionDate(request.productionDate());
        sensor.setCompany(sensorCompany);
        sensor.setIsActive(true);

        Sensor savedSensor = sensorRepository.saveAndFlush(sensor);

        if (savedSensor.getIsActive()) {
            registerSensorWithGenerator(savedSensor);
        }

        return SensorDto.toDto(savedSensor);
    }

    private void registerSensorWithGenerator(Sensor sensor) {
        if (sensor == null || !sensor.getIsActive()) {
            return;
        }
        UUID technicId = (sensor.getTechnic() != null) ? sensor.getTechnic().getId() : null;

        RegisterSensorToGeneratorRequest registrationRequest = RegisterSensorToGeneratorRequest.builder()
                .sensorId(sensor.getId())
                .technicId(technicId)
                .sensorType(sensor.getType() != null ? sensor.getType().getName() : "UNKNOWN") // Убедись, что у SensorType есть getName()
                .serialNumber(sensor.getSerialNumber())
                .minValue(parseSafeDouble(sensor.getMinValue()))
                .maxValue(parseSafeDouble(sensor.getMaxValue()))
                .build();

        sensorGeneratorClient.registerSensor(registrationRequest);
    }

    private Double parseSafeDouble(String value) {
        if (value == null || value.isBlank()) return null;

        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }

    }

    @Override
    @Transactional
    public void assignToTechnic(SensorAssignmentRequest request) {
        Sensor sensor = sensorRepository.findByIdOrThrow(request.sensorId());
        Technic technic = technicService.getEntityById(request.technicId());

        if (technic.getSensors().stream().anyMatch(s -> s.getId().equals(sensor.getId()))) {
            throw new EntityDuplicationException("Sensor already assigned to this technic");
        }

        technic.getSensors().add(sensor);
        sensor.setTechnic(technic);
        sensor.setIsActive(true);
        sensor.setCompany(technic.getCompany());
        Sensor savedSensor = sensorRepository.saveAndFlush(sensor);
        technicService.save(technic);

        if (savedSensor.getIsActive()) {
            registerSensorWithGenerator(savedSensor);
        }
    }


    @Override
    @Transactional
    public void unassignFromTechnic(SensorAssignmentRequest request) {
        Sensor sensor = sensorRepository.findByIdOrThrow(request.sensorId());

        Technic technic = technicService.getEntityById(request.technicId());

        if (sensor.getTechnic() == null || !technic.equals(sensor.getTechnic())) {
            throw new SensorBadRequestException("Sensor is not assigned to this technic");
        }

        technic.getSensors().remove(sensor);
        sensor.setTechnic(null);
        sensor.setCompany(null);
        sensor.setIsActive(false);

        technicService.save(technic);
        Sensor savedSensor = sensorRepository.saveAndFlush(sensor);

        if (savedSensor.getIsActive()) {
            registerSensorWithGenerator(savedSensor);
        } else {
            sensorGeneratorClient.deregisterSensor(savedSensor.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SensorsDto getAllCompanySensors(String email) {
        User manager = userService.getByEmail(email);

        List<Sensor> sensors = sensorRepository.findAllByCompanyId(manager.getCompany().getId());

        return new SensorsDto(
                sensors.stream().map(SensorDto::toDto).toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SensorsDto getAllSensorsByCompanyIdForAdmin(UUID companyId) {
        if (!companyService.isExistsById(companyId)) {
            throw new ResourceNotFoundException("Company with id " + companyId + " not found");
        }

        List<Sensor> sensors = sensorRepository.findAllByCompanyId(companyId);

        return new SensorsDto(
                sensors.stream().map(SensorDto::toDto).toList()
        );
    }

    @Override
    public SensorsDto getSensorsForDriver() {
        User user = securityService.getCurrentUser();

        List<Technic> userTechnics = user.getTechnics();

        if (userTechnics == null || userTechnics.isEmpty()) {
            return new SensorsDto(Collections.emptyList());
        }

        List<SensorDto> allSensorDtos = userTechnics.stream()
                .flatMap(technic -> technic.getSensors().stream())
                .map(SensorDto::toDto)
                .collect(Collectors.toList());

        return new SensorsDto(allSensorDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorsDto getSensorsByTechnicId(UUID technicId) {
        User user = securityService.getCurrentUser();
        Technic technic = technicService.getEntityById(technicId);
        boolean authorized = isAuthorized(user, technic);

        if (!authorized) {
            throw new AccessDeniedException("User does not have permission to access sensors for this technic.");
        }

        List<SensorDto> sensorDtos = technic.getSensors().stream()
                .map(SensorDto::toDto)
                .collect(Collectors.toList());
        return new SensorsDto(sensorDtos);
    }

    @Override
    @Transactional
    public SensorDto updateSensor(UUID sensorId, UpdateSensorRequest request) {
        Sensor sensor = sensorRepository.findByIdOrThrow(sensorId);
        boolean previousActiveState = sensor.getIsActive();

        if (request.serialNumber() != null && !request.serialNumber().equals(sensor.getSerialNumber())) {
            sensorRepository.throwIfExistsWithSameSerialNumber(request.serialNumber());
            sensor.setSerialNumber(request.serialNumber());
        }
        if (request.manufacturer() != null) {
            sensor.setManufacturer(request.manufacturer());
        }
        if (request.minValue() != null) {
            sensor.setMinValue(request.minValue());
        }
        if (request.maxValue() != null) {
            sensor.setMaxValue(request.maxValue());
        }
        if (request.productionDate() != null) {
            sensor.setProductionDate(request.productionDate());
        }
        if (request.calibrationDueDate() != null) {
            sensor.setCalibrationDueDate(request.calibrationDueDate());
        }
        if (request.isActive() != null) {
            sensor.setIsActive(request.isActive());
        }

        Sensor updatedSensor = sensorRepository.saveAndFlush(sensor);

        if (previousActiveState != updatedSensor.getIsActive()) {
            if (updatedSensor.getIsActive()) {
                registerSensorWithGenerator(updatedSensor);
            } else {
                sensorGeneratorClient.deregisterSensor(updatedSensor.getId());
            }
        } else if (updatedSensor.getIsActive()) {
            registerSensorWithGenerator(updatedSensor);
        }

        return SensorDto.toDto(updatedSensor);
    }


    @Override
    @Transactional
    public void deleteSensor(UUID sensorId) {
        Sensor sensor = sensorRepository.findByIdOrThrow(sensorId);

        if (sensor.getTechnic() != null) {
            throw new SensorBadRequestException(
                    "Cannot delete sensor. It is currently assigned to technic with ID: " +
                            sensor.getTechnic().getId() + ". Please unassign it first."
            );
        }

        sensorGeneratorClient.deregisterSensor(sensorId);

        sensorRepository.delete(sensor);
    }

    @Override
    public boolean existsByTypeId(UUID typeId) {
        return sensorRepository.existsByType_Id(typeId);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorsDto getSensorsBySensorTypeId(UUID sensorTypeId) {
        List<Sensor> sensors = sensorRepository.findAllByType_Id(sensorTypeId);
        return new SensorsDto(sensors.stream().map(SensorDto::toDto).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Sensor getSensorEntityById(UUID sensorId) {
        return sensorRepository.findByIdOrThrow(sensorId);
    }

    private static boolean isAuthorized(User user, Technic technic) {
        RoleName roleName = user.getRole();
        boolean authorized = false;

        if (roleName.equals(RoleName.ROLE_ADMIN)) {
            authorized = true;
        } else if (roleName.equals(RoleName.ROLE_MANAGER)) {
            if (technic.getCompany() != null && user.getCompany() != null &&
                    technic.getCompany().getId().equals(user.getCompany().getId())) {
                authorized = true;
            }
        } else if (roleName.equals(RoleName.ROLE_DRIVER)) {
            if (technic.getOwnerId() != null && technic.getOwnerId().getId().equals(user.getId())) {
                authorized = true;
            }
        }
        return authorized;
    }

    @Override
    @Transactional(readOnly = true)
    public SensorsDto getAllSensorsPaged(Boolean attachedToTechnic, Pageable pageable) {
        Page<Sensor> sensorPage;

        if (attachedToTechnic == null) {
            sensorPage = sensorRepository.findAll(pageable);
        } else if (Boolean.TRUE.equals(attachedToTechnic)) {
            sensorPage = sensorRepository.findByTechnicIsNotNull(pageable);
        } else {
            sensorPage = sensorRepository.findByTechnicIsNull(pageable);
        }

        List<SensorDto> dtoList = sensorPage.getContent().stream()
                .map(SensorDto::toDto)
                .collect(Collectors.toList());

        return new SensorsDto(dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorDataHistoryDto> getSensorDataHistory(
            UUID sensorId,
            LocalDateTime from,
            LocalDateTime to,
            DataGranularity granularity,
            AggregationType aggregationType
    ) {
        Sensor sensor = sensorRepository.findByIdOrThrow(sensorId);
        User user = securityService.getCurrentUser();
        RoleName roleName = user.getRole();
        boolean authorized = false;

        if (roleName.equals(RoleName.ROLE_ADMIN)) {
            authorized = true;
        } else if (roleName.equals(RoleName.ROLE_MANAGER)) {
            if (sensor.getCompany() != null && user.getCompany() != null &&
                    sensor.getCompany().getId().equals(user.getCompany().getId())) {
                authorized = true;
            }
        }

        if (!authorized) {
            throw new AccessDeniedException("User does not have permission to access sensors for this technic.");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date.");
        }

        if (granularity == null || granularity == DataGranularity.RAW) {
            List<SensorData> rawData = sensorDataRepository.findBySensorIdAndTimestampBetweenOrderByTimestampAsc(sensorId, from, to);
            return rawData.stream()
                    .map(this::mapSensorDataToRawHistoryDto)
                    .collect(Collectors.toList());
        } else {
            List<Object[]> nativeResults;
            String granularityStr = granularity.name().toLowerCase();
            AggregationType aggTypeToUse = aggregationType != null ? aggregationType : AggregationType.AVG;

            nativeResults = switch (aggTypeToUse) {
                case AVG ->
                        sensorDataRepository.findNativeAggregatedAvgByGranularity(sensorId, from, to, granularityStr);
                case MIN ->
                        sensorDataRepository.findNativeAggregatedMinByGranularity(sensorId, from, to, granularityStr);
                case MAX ->
                        sensorDataRepository.findNativeAggregatedMaxByGranularity(sensorId, from, to, granularityStr);
                case SUM ->
                        sensorDataRepository.findNativeAggregatedSumByGranularity(sensorId, from, to, granularityStr);
                case COUNT ->
                        sensorDataRepository.findNativeAggregatedCountByGranularity(sensorId, from, to, granularityStr);
                case LAST ->
                        sensorDataRepository.findNativeAggregatedLastByGranularity(sensorId, from, to, granularityStr);
                case FIRST ->
                        sensorDataRepository.findNativeAggregatedFirstByGranularity(sensorId, from, to, granularityStr);
                default -> throw new IllegalArgumentException("Unsupported aggregation type: " + aggTypeToUse);
            };

            List<AggregatedSensorDataViewImpl> aggregatedViews = mapNativeResultsToView(nativeResults, aggTypeToUse);

            return aggregatedViews.stream()
                    .map(this::mapAggregatedViewToHistoryDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SensorValueStatsDto getSensorValueStatisticsForPeriod(
            UUID sensorId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found with id: " + sensorId);
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date.");
        }

        Double minValue = sensorDataRepository.findMinValueInPeriod(sensorId, from, to).orElse(null);
        Double maxValue = sensorDataRepository.findMaxValueInPeriod(sensorId, from, to).orElse(null);
        Double avgValue = sensorDataRepository.findAvgValueInPeriod(sensorId, from, to).orElse(null);

        Double lastValue = null;
        Optional<Object[]> lastValueAndStatusOpt = sensorDataRepository.findLastValueAndStatusInPeriod(sensorId, from, to);
        if (lastValueAndStatusOpt.isPresent()) {
            Object[] lastData = lastValueAndStatusOpt.get();
            if (lastData[0] instanceof Object[] array) {
                lastValue = (Double) array[0];
            }
        }

        return SensorValueStatsDto.builder()
                .minValue(minValue)
                .maxValue(maxValue)
                .avgValue(avgValue)
                .lastValue(lastValue)
                .build();
    }

    private List<AggregatedSensorDataViewImpl> mapNativeResultsToView(List<Object[]> nativeResults, AggregationType aggType) {
        return nativeResults.stream().map(row -> {
            LocalDateTime timestamp = null;
            if (row[0] instanceof Timestamp) {
                timestamp = ((Timestamp) row[0]).toLocalDateTime();
            } else if (row[0] instanceof LocalDateTime) {
                timestamp = (LocalDateTime) row[0];
            }


            Double value = null;
            if (row[1] instanceof Number) {
                value = ((Number) row[1]).doubleValue();
            } else if (row[1] != null) {
                try {
                    value = Double.parseDouble(row[1].toString());
                } catch (NumberFormatException e) {
                    log.warn("Cannot parse value from native query: {}", row[1]);
                }
            }

            SensorStatus status = null;
            if ((aggType == AggregationType.FIRST || aggType == AggregationType.LAST) && row.length > 2 && row[2] != null) {
                if (row[2] instanceof String) {
                    try {
                        status = SensorStatus.valueOf(((String) row[2]).toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Cannot parse status '{}' from native query", row[2]);
                        status = SensorStatus.UNDEFINED;
                    }
                } else if (row[2] instanceof SensorStatus) {
                    status = (SensorStatus) row[2];
                }
            }
            return new AggregatedSensorDataViewImpl(timestamp, value, status);
        }).collect(Collectors.toList());
    }

    private SensorDataHistoryDto mapSensorDataToRawHistoryDto(SensorData data) {
        Double numericValue = null;
        if (data.getValue() != null && !data.getValue().isBlank()) {
            numericValue = Double.parseDouble(data.getValue().replace(',', '.'));
        }
        return SensorDataHistoryDto.builder()
                .timestamp(data.getTimestamp())
                .value(numericValue)
                .status(data.getStatus())
                .build();
    }

    private SensorDataHistoryDto mapAggregatedViewToHistoryDto(AggregatedSensorDataView aggregatedView) {
        return SensorDataHistoryDto.builder()
                .timestamp(aggregatedView.getIntervalStart())
                .value(aggregatedView.getAggregatedValue())
                .status(aggregatedView.getAggregatedStatus())
                .build();
    }

    public interface AggregatedSensorDataView {
        LocalDateTime getIntervalStart();

        Double getAggregatedValue();

        SensorStatus getAggregatedStatus();
    }

}
