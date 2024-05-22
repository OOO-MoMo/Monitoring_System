package ru.momo.monitoring.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorToTechnicRequestDto {

    @NotNull(message = "Sensor id must be not null")
    Long sensorId;

    @NotNull(message = "Technic id must be not null")
    Long technicId;

}
