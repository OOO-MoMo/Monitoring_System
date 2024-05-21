package ru.momo.monitoring.store.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Sensor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorResponseDto {

    String type;

    String dataType;

    public static SensorResponseDto mapFromEntity(Sensor sensor) {
        return SensorResponseDto.builder()
                .type(sensor.getType())
                .dataType(sensor.getDataType())
                .build();
    }

}
