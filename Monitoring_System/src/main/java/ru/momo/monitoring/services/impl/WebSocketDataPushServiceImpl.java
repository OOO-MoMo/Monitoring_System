package ru.momo.monitoring.services.impl;

import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.WebSocketDataPushService;
import ru.momo.monitoring.store.dto.response.SensorDataRealtimeDto;

import java.util.UUID;

@Service
public class WebSocketDataPushServiceImpl implements WebSocketDataPushService {

    @Override
    public void pushSpecificSensorData(UUID id, SensorDataRealtimeDto realtimeDto) {

    }

}
