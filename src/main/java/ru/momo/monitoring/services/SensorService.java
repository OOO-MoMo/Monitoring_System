package ru.momo.monitoring.services;

import jakarta.validation.Valid;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.request.SensorAssignmentRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.dto.response.SensorsDto;

import java.util.UUID;

public interface SensorService {

    SensorDto registerSensor(@Valid CreateSensorRequest request, String email);

    void assignToTechnic(@Valid SensorAssignmentRequest request, String email);

    void unassignFromTechnic(@Valid SensorAssignmentRequest request, String email);

    SensorsDto getAllCompanySensors(String email);

    SensorsDto getAllSensorsByCompanyIdForAdmin(UUID companyId);

}
