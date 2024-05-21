package ru.momo.monitoring.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.store.dto.request.SensorCreateRequestDto;
import ru.momo.monitoring.store.dto.response.SensorCreatedResponseDto;

import java.net.URI;

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

    @PostMapping("/")
    public ResponseEntity<?> addNewSensor(@RequestBody @Validated SensorCreateRequestDto request) {
        SensorCreatedResponseDto response = sensorService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/sensors/" + response.getSensorId()))
                .body(response);
    }
}
