package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorCreatedResponseDto {

    Long sensorId;

    String type;

    String dataType;
/*
    public static SensorCreatedResponseDto mapFromEntity(Sensor sensor) {
        return SensorCreatedResponseDto
                .builder()
                .sensorId(sensor.getSensorId())
                .type(sensor.getType())
                .dataType(sensor.getDataType())
                .build();
    }*/

}
