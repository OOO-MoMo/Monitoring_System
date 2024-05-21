package ru.momo.monitoring.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.momo.monitoring.store.entities.Sensor;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorCreateRequestDto {

    @NotNull(message = "Type must be not null")
    @Length(max = 255, message = "Type length must be smaller than 255 symbols")
    String type;

    @NotNull(message = "Data type must be not null")
    @Length(max = 255, message = "Data type length must be smaller than 255 symbols")
    String dataType;

    public static Sensor mapToEntity(SensorCreateRequestDto request) {
        return Sensor
                .builder()
                .data("0")
                .type(request.getType())
                .dataType(request.getDataType())
                .build();
    }
}
