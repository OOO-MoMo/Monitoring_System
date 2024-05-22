package ru.momo.monitoring.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.store.dto.request.SensorCreateRequestDto;
import ru.momo.monitoring.store.dto.request.SensorToTechnicRequestDto;
import ru.momo.monitoring.store.dto.response.SensorCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.SensorResponseDto;
import ru.momo.monitoring.store.dto.response.SensorToTechnicResponseDto;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sensors")
public class SensorController {

    private final SensorService sensorService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(sensorService.getSensorById(id));
    }

    @GetMapping("/")
    public ResponseEntity<?> getByTechnicId(
            @RequestParam(name = "technicId") Long technicId) {
        List<SensorResponseDto> response = sensorService.getSensorByTechnicId(technicId);

        if (response.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> addNewSensor(@RequestBody @Validated SensorCreateRequestDto request) {
        SensorCreatedResponseDto response = sensorService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/sensors/" + response.getSensorId()))
                .body(response);
    }

    @PutMapping("/")
    public ResponseEntity<?> actionSensorToTechnic(@RequestBody @Validated SensorToTechnicRequestDto request) {
        SensorToTechnicResponseDto response = sensorService.actionSensorToTechnic(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
