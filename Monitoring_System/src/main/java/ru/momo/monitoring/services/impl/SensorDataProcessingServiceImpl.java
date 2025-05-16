package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.SensorDataProcessingService;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.WebSocketDataPushService;
import ru.momo.monitoring.store.dto.data_generator.GeneratedSensorDataDto;
import ru.momo.monitoring.store.dto.response.SensorDataRealtimeDto;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.SensorData;
import ru.momo.monitoring.store.entities.enums.SensorStatus;
import ru.momo.monitoring.store.repositories.SensorDataRepository;

@Service
@RequiredArgsConstructor
public class SensorDataProcessingServiceImpl implements SensorDataProcessingService {

    private final SensorDataRepository sensorDataRepository;

    private final SensorService sensorService;

    private final WebSocketDataPushService webSocketDataPushService;

    @Value("${sensor.generator.service.threshold}")
    private double WARNING_THRESHOLD_PERCENTAGE;

    @Override
    public void processIncomingData(GeneratedSensorDataDto incomingData) {
        Sensor currentSensor = sensorService.getSensorEntityById(incomingData.getSensorId());

        if (!currentSensor.getIsActive()) {
            return;
        }

        Double currentValue = parseDouble(incomingData.getValue());
        if (currentValue == null) {
            saveAndPushData(currentSensor, incomingData, SensorStatus.UNDEFINED);
            return;
        }

        SensorStatus status = determineSensorStatus(currentSensor, currentValue);

        saveAndPushData(currentSensor, incomingData, status);
    }

    private void saveAndPushData(Sensor sensor, GeneratedSensorDataDto incomingData, SensorStatus status) {
        SensorData newSensorDataEntry = new SensorData();
        newSensorDataEntry.setSensor(sensor);

        if (sensor.getTechnic() != null) {
            newSensorDataEntry.setTechnic(sensor.getTechnic());
        }

        newSensorDataEntry.setValue(incomingData.getValue());
        newSensorDataEntry.setTimestamp(incomingData.getTimestamp());
        newSensorDataEntry.setStatus(status);

        sensorDataRepository.save(newSensorDataEntry);

        SensorDataRealtimeDto realtimeDto = SensorDataRealtimeDto.builder()
                .sensorId(sensor.getId())
                .technicId(sensor.getTechnic() != null ? sensor.getTechnic().getId() : null)
                .sensorSerialNumber(sensor.getSerialNumber())
                .value(newSensorDataEntry.getValue())
                .timestamp(newSensorDataEntry.getTimestamp())
                .status(newSensorDataEntry.getStatus())
                .sensorType(sensor.getType().getName())
                .unitOfMeasurement(sensor.getType().getUnit())
                .build();

        webSocketDataPushService.pushSpecificSensorData(sensor.getId(), realtimeDto);
    }

    private Double parseDouble(String stringValue) {
        if (stringValue == null || stringValue.isBlank()) {
            return null;
        }

        return Double.parseDouble(stringValue.replace(',', '.'));
    }

    private SensorStatus determineSensorStatus(Sensor sensor, double currentValue) {
        Double minValue = parseDouble(sensor.getMinValue());
        Double maxValue = parseDouble(sensor.getMaxValue());

        if (currentValue < minValue || currentValue > maxValue) {
            return SensorStatus.CRITICAL;
        }

        double range = maxValue - minValue;
        if (range * (1 - 2 * WARNING_THRESHOLD_PERCENTAGE) > 0) {
            double warningOffset = range * WARNING_THRESHOLD_PERCENTAGE;
            double lowerWarningLimit = minValue + warningOffset;
            double upperWarningLimit = maxValue - warningOffset;

            if (currentValue < lowerWarningLimit || currentValue > upperWarningLimit) {
                return SensorStatus.WARNING;
            }
        }

        return SensorStatus.NORMAL;
    }


}
