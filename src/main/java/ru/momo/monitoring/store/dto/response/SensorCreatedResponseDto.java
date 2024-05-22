package ru.momo.monitoring.store.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Sensor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorCreatedResponseDto {

    Long sensorId;

    String type;

    String dataType;

    public static SensorCreatedResponseDto mapFromEntity(Sensor sensor) {
        return SensorCreatedResponseDto
                .builder()
                .sensorId(sensor.getSensorId())
                .type(sensor.getType())
                .dataType(sensor.getDataType())
                .build();
    }

}
