package ru.momo.monitoring.store.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.dto.request.SensorToTechnicRequestDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorToTechnicResponseDto {

    Long sensorId;

    Long technicId;

    String action;

    public SensorToTechnicResponseDto(SensorToTechnicRequestDto request) {
        this.sensorId = request.getSensorId();
        this.technicId = request.getTechnicId();
        this.action = "Was " + request.getAction() + "ed";
    }
}
