package ru.momo.monitoring.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.services.SensorDataProcessingService;
import ru.momo.monitoring.store.dto.data_generator.GeneratedSensorDataDto;

@RestController
@RequestMapping("/api/v1/internal/sensor-data")
@RequiredArgsConstructor
@Slf4j
public class SensorDataReceiverController {

    private final SensorDataProcessingService sensorDataProcessingService;

    @PostMapping("/receive")
    @ResponseStatus(HttpStatus.OK)
    public void receiveSensorData(@RequestBody GeneratedSensorDataDto data) {
        sensorDataProcessingService.processIncomingData(data);
    }

}