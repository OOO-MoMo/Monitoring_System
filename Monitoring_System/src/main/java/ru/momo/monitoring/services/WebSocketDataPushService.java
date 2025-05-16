package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.response.SensorDataRealtimeDto;

import java.util.UUID;

public interface WebSocketDataPushService {

    void pushSpecificSensorData(UUID id, SensorDataRealtimeDto realtimeDto);

}
