package ru.momo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSensorRequest {

    private UUID sensorId;

    private UUID technicId;

    private String sensorType;

    private String serialNumber;

    private Double minValue;

    private Double maxValue;

}