package ru.momo.monitoring.store.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorToTechnicRequestDto {

    public final static String ATTACH = "attach";

    public final static String UNPIN = "unpin";

    @NotNull(message = "Action must be not null")
    @Pattern(regexp = "^(attach|unpin)$", message = "Action must be either 'attach' or 'unpin'")
    String action;

    @NotNull(message = "Sensor id must be not null")
    Long sensorId;

    @NotNull(message = "Technic id must be not null")
    UUID technicId;

}
