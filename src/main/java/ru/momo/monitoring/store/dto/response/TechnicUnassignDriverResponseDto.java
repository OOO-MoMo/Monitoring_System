package ru.momo.monitoring.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicUnassignDriverResponseDto {
    private UUID technicId;
    private UUID driverId;
    private String message;
}