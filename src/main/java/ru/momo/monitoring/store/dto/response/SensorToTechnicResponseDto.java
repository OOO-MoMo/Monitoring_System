package ru.momo.monitoring.store.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.dto.request.SensorToTechnicRequestDto;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorToTechnicResponseDto {

    Long sensorId;

    UUID technicId;

    String action;

    public SensorToTechnicResponseDto(SensorToTechnicRequestDto request) {
        this.sensorId = request.getSensorId();
        this.technicId = request.getTechnicId();
        this.action = "Was " + request.getAction() + "ed";
    }
}
