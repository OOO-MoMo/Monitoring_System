package ru.momo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.momo.dto.GeneratedSensorDataDto;
import ru.momo.dto.RegisterSensorRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeneratorOrchestrationService {

    private final Map<UUID, RegisterSensorRequest> registeredSensors = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final RestTemplate restTemplate;

    @Value("${monitoring.service.data-receiver.url}")
    private String dataReceiverUrl;

    public boolean registerSensor(RegisterSensorRequest request) {
        registeredSensors.put(request.getSensorId(), request);

        log.info(
                "Sensor registered/updated for data generation: ID={}, Type={}, Serial={}",
                request.getSensorId(),
                request.getSensorType(),
                request.getSerialNumber()
        );

        return true;
    }

    public boolean deregisterSensor(UUID sensorId) {
        RegisterSensorRequest removed = registeredSensors.remove(sensorId);

        if (removed != null) {
            log.info("Sensor deregistered: ID={}", sensorId);
            return true;
        } else {
            log.warn("Attempted to deregister non-existent sensor: ID={}", sensorId);
            return false;
        }
    }

    @Scheduled(fixedRateString = "${generator.schedule.fixedRate.ms:5000}")
    public void generateAndSendData() {
        if (registeredSensors.isEmpty()) {
            log.trace("No sensors registered for data generation.");
            return;
        }

        log.debug("Scheduled data generation for {} sensors.", registeredSensors.size());
        for (Map.Entry<UUID, RegisterSensorRequest> entry : registeredSensors.entrySet()) {
            RegisterSensorRequest sensorInfo = entry.getValue();

            GeneratedSensorDataDto dataToPush = generateDataForSensor(sensorInfo);

            sendDataToMonitoringService(dataToPush, sensorInfo.getSensorId());
        }
    }

    private GeneratedSensorDataDto generateDataForSensor(RegisterSensorRequest sensorInfo) {
        double value = sensorInfo.getMinValue() + (
                sensorInfo.getMaxValue() - sensorInfo.getMinValue()
        ) * random.nextDouble();

        return GeneratedSensorDataDto.builder()
                .sensorId(sensorInfo.getSensorId())
                .technicId(sensorInfo.getTechnicId())
                .value(String.format("%.2f", value))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void sendDataToMonitoringService(GeneratedSensorDataDto dataToPush, UUID sensorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeneratedSensorDataDto> requestEntity = new HttpEntity<>(dataToPush, headers);

        try {
            restTemplate.postForEntity(dataReceiverUrl, requestEntity, Void.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error sending data for sensor {} to {}. Status: {}, Body: {}",
                    sensorId, dataReceiverUrl, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("Network error sending data for sensor {} to {}: {}", sensorId, dataReceiverUrl, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending data for sensor {} to {}: {}", sensorId, dataReceiverUrl, e.getMessage(), e);
        }
    }

}