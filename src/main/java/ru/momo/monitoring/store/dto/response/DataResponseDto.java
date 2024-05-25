package ru.momo.monitoring.store.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.sensor.AbstractSensor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataResponseDto {

    String data;

    public static DataResponseDto mapFromEntity(AbstractSensor sensor) {
        return DataResponseDto
                .builder()
                .data(sensor.calculateData())
                .build();
    }

}
