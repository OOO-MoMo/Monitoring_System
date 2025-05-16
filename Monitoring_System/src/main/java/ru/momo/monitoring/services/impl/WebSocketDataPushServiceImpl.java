package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.WebSocketDataPushService;
import ru.momo.monitoring.store.dto.response.SensorDataRealtimeDto;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketDataPushServiceImpl implements WebSocketDataPushService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Отправляет данные конкретного сенсора всем клиентам, подписанным на топик этого сенсора.
     *
     * @param sensorId    ID сенсора, для которого предназначены данные.
     * @param realtimeDto DTO с данными сенсора.
     */
    @Override
    public void pushSpecificSensorData(UUID sensorId, SensorDataRealtimeDto realtimeDto) {
        if (sensorId == null || realtimeDto == null) {
            log.warn("Cannot push WebSocket data: sensorId or DTO is null.");
            return;
        }

        String destination = String.format("/topic/sensor/%s/data", sensorId);

        try {
            log.debug("Pushing data to WebSocket destination '{}': {}", destination, realtimeDto);
            messagingTemplate.convertAndSend(destination, realtimeDto);
            getTrace(destination);
        } catch (Exception e) {
            log.error("Error pushing data to WebSocket destination '{}': {}", destination, e.getMessage(), e);
        }
    }

    private static void getTrace(String destination) {
        log.trace("Successfully pushed data to {}", destination);
    }

}
