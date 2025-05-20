package ru.momo.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

    private final Map<UUID, SensorState> registeredSensorsWithState = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final RestTemplate restTemplate;

    @Value("${monitoring.service.data-receiver.url}")
    private String dataReceiverUrl;

    @Value("${generator.smooth.max-step-percentage}")
    private double maxStepPercentage;

    @Value("${generator.event.warning.chance}")
    private double warningEventChance;
    @Value("${generator.event.warning.duration.ticks}")
    private int warningEventDurationTicks;

    @Value("${generator.event.critical.chance}")
    private double criticalEventChance;
    @Value("${generator.event.critical.duration.ticks}")
    private int criticalEventDurationTicks;


    public boolean registerSensor(RegisterSensorRequest request) {
        if (request.getSensorId() == null) {
            log.warn("Attempted to register sensor with null ID.");
            throw new IllegalArgumentException("Sensor ID cannot be null for registration.");
        }

        SensorState state = new SensorState(request);
        if (request.getMinValue() != null && request.getMaxValue() != null && request.getMinValue() < request.getMaxValue()) {
            state.setLastValue((request.getMinValue() + request.getMaxValue()) / 2.0);
        } else if (request.getMinValue() != null) {
            state.setLastValue(request.getMinValue());
        } else {
            state.setLastValue(50.0);
        }

        registeredSensorsWithState.put(request.getSensorId(), state);
        log.info("Sensor registered/updated for data generation: ID={}, Type={}, Serial={}, InitialValue={}",
                request.getSensorId(), request.getSensorType(), request.getSerialNumber(), state.getLastValue());
        return true;
    }

    public boolean deregisterSensor(UUID sensorId) {
        if (sensorId == null) {
            log.warn("Attempted to deregister sensor with null ID.");
            return false;
        }
        SensorState removed = registeredSensorsWithState.remove(sensorId);
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
        if (registeredSensorsWithState.isEmpty()) {
            return;
        }

        log.debug("Scheduled data generation for {} sensors.", registeredSensorsWithState.size());
        for (Map.Entry<UUID, SensorState> entry : registeredSensorsWithState.entrySet()) {
            SensorState sensorState = entry.getValue();
            RegisterSensorRequest sensorInfo = sensorState.getSensorInfo();

            GeneratedSensorDataDto dataToPush = generateDataForSensor(sensorState);
            sendDataToMonitoringService(dataToPush, sensorInfo.getSensorId());
        }
    }

    private GeneratedSensorDataDto generateDataForSensor(SensorState state) {
        RegisterSensorRequest info = state.getSensorInfo();
        double currentValue = state.getLastValue();
        String statusOverride = null;

        if (state.getEventTicksRemaining() > 0) {
            state.decrementEventTicks();
            statusOverride = state.getCurrentEventType();
        } else {
            state.setCurrentEventType(null);

            if (random.nextDouble() < criticalEventChance) {
                state.startEvent("CRITICAL", criticalEventDurationTicks);
                statusOverride = "CRITICAL";
            } else if (random.nextDouble() < warningEventChance) {
                state.startEvent("WARNING", warningEventDurationTicks);
                statusOverride = "WARNING";
            }
        }

        if ("CRITICAL".equals(statusOverride)) {

            if (info.getMinValue() != null && info.getMaxValue() != null && info.getMinValue() < info.getMaxValue()) {
                double range = info.getMaxValue() - info.getMinValue();
                if (random.nextBoolean()) {
                    currentValue = info.getMaxValue() + (random.nextDouble() * range * 0.1 + 0.01 * range);
                } else {
                    currentValue = info.getMinValue() - (random.nextDouble() * range * 0.1 + 0.01 * range);
                }
            } else {
                currentValue = (random.nextBoolean() ? 1 : -1) * (100 + random.nextDouble() * 50);
            }
        } else if ("WARNING".equals(statusOverride)) {
            if (info.getMinValue() != null && info.getMaxValue() != null && info.getMinValue() < info.getMaxValue()) {
                double range = info.getMaxValue() - info.getMinValue();
                double warningOffset = range * 0.15;
                if (random.nextBoolean()) {
                    currentValue = info.getMaxValue() - (random.nextDouble() * warningOffset * 0.5);
                } else {
                    currentValue = info.getMinValue() + (random.nextDouble() * warningOffset * 0.5);
                }
            } else {
                currentValue = currentValue + (random.nextBoolean() ? 1 : -1) * (random.nextDouble() * 10 + 5);
            }
        } else {
            if (info.getMinValue() != null && info.getMaxValue() != null && info.getMinValue() < info.getMaxValue()) {
                double range = info.getMaxValue() - info.getMinValue();
                double step = (random.nextDouble() - 0.5) * 2.0 * (range * maxStepPercentage);
                currentValue += step;
                currentValue = Math.max(info.getMinValue(), Math.min(info.getMaxValue(), currentValue));
                log.info("Current value(NORMAL)={}", currentValue);
            } else {
                currentValue += (random.nextDouble() - 0.5) * 2.0 * 5.0;
            }
        }

        state.setLastValue(currentValue);

        return GeneratedSensorDataDto.builder()
                .sensorId(info.getSensorId())
                .technicId(info.getTechnicId())
                .value(String.format("%.2f", currentValue))
                .timestamp(LocalDateTime.now())
                .build();
    }


    private void sendDataToMonitoringService(GeneratedSensorDataDto dataToPush, UUID sensorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeneratedSensorDataDto> requestEntity = new HttpEntity<>(dataToPush, headers);

        try {
            restTemplate.postForEntity(dataReceiverUrl, requestEntity, Void.class);
            log.trace("Successfully sent data for sensor {}", sensorId);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error sending data for sensor {} to {}. Status: {}, Body: {}",
                    sensorId, dataReceiverUrl, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("Network error sending data for sensor {} to {}: {}", sensorId, dataReceiverUrl, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending data for sensor {} to {}: {}", sensorId, dataReceiverUrl, e.getMessage(), e);
        }
    }

    @Getter
    @Setter
    private static class SensorState {
        private final RegisterSensorRequest sensorInfo;
        private double lastValue;
        private int eventTicksRemaining;
        private String currentEventType;

        public SensorState(RegisterSensorRequest sensorInfo) {
            this.sensorInfo = sensorInfo;
            this.eventTicksRemaining = 0;
        }

        public void startEvent(String eventType, int durationTicks) {
            this.currentEventType = eventType;
            this.eventTicksRemaining = durationTicks;
        }

        public void decrementEventTicks() {
            if (this.eventTicksRemaining > 0) {
                this.eventTicksRemaining--;
            }
        }
    }

}