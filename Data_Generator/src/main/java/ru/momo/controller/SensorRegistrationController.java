package ru.momo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.dto.RegisterSensorRequest;
import ru.momo.service.GeneratorOrchestrationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/generator/sensors")
@RequiredArgsConstructor
@Slf4j
public class SensorRegistrationController {

    private final GeneratorOrchestrationService generatorService;

    @PostMapping("/register")
    public ResponseEntity<String> registerSensor(@RequestBody RegisterSensorRequest request) {
        log.info("Received registration request for sensor: {}", request);
        try {
            generatorService.registerSensor(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Sensor " + request.getSensorId() + " registered successfully.");
        } catch (IllegalArgumentException e) {
            log.error("Bad request for sensor registration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Internal server error during sensor registration for sensorId {}: {}", request.getSensorId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to register sensor.");
        }
    }

    @DeleteMapping("/{sensorId}/deregister")
    public ResponseEntity<String> deregisterSensor(@PathVariable UUID sensorId) {
        log.info("Received deregistration request for sensor ID: {}", sensorId);
        boolean success = generatorService.deregisterSensor(sensorId);
        if (success) {
            return ResponseEntity.ok("Sensor " + sensorId + " deregistered successfully.");
        } else {
            return ResponseEntity.ok("Deregistration processed for sensor " + sensorId + " (it might not have been registered).");
        }
    }

}