package ru.momo.monitoring.store.dto.data_generator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterSensorToGeneratorRequest {
    private UUID sensorId;
    private UUID technicId;
    private String sensorType;
    private String serialNumber;
    private Double minValue;
    private Double maxValue;
}