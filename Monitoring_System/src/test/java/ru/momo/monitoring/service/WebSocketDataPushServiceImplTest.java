package ru.momo.monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.momo.monitoring.services.impl.WebSocketDataPushServiceImpl;
import ru.momo.monitoring.store.dto.response.SensorDataRealtimeDto;
import ru.momo.monitoring.store.entities.enums.SensorStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketDataPushServiceImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketDataPushServiceImpl webSocketDataPushService;

    @Test
    void pushSpecificSensorData_WhenValidInput_ShouldCallConvertAndSendWithCorrectParameters() {
        // Arrange
        UUID sensorId = UUID.randomUUID();
        SensorDataRealtimeDto dto = SensorDataRealtimeDto.builder()
                .sensorId(sensorId)
                .value("25.5")
                .timestamp(LocalDateTime.now())
                .status(SensorStatus.NORMAL)
                .build();
        String expectedDestination = "/topic/sensor/" + sensorId + "/data";

        // Act
        webSocketDataPushService.pushSpecificSensorData(sensorId, dto);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertEquals(expectedDestination, destinationCaptor.getValue());
        assertEquals(dto, payloadCaptor.getValue());
    }

    @Test
    void pushSpecificSensorData_WhenSensorIdIsNull_ShouldLogWarningAndNotCallConvertAndSend() {
        // Arrange
        SensorDataRealtimeDto dto = SensorDataRealtimeDto.builder().value("22.0").build();

        // Act
        webSocketDataPushService.pushSpecificSensorData(null, dto);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void pushSpecificSensorData_WhenDtoIsNull_ShouldLogWarningAndNotCallConvertAndSend() {
        // Arrange
        UUID sensorId = UUID.randomUUID();

        // Act
        webSocketDataPushService.pushSpecificSensorData(sensorId, null);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void pushSpecificSensorData_WhenConvertAndSendThrowsException_ShouldLogErrorAndNotPropagateException() {
        // Arrange
        UUID sensorId = UUID.randomUUID();
        SensorDataRealtimeDto dto = SensorDataRealtimeDto.builder()
                .sensorId(sensorId)
                .value("25.5")
                .timestamp(LocalDateTime.now())
                .status(SensorStatus.NORMAL)
                .build();
        String expectedDestination = "/topic/sensor/" + sensorId + "/data";

        doThrow(new RuntimeException("Simulated messaging error"))
                .when(messagingTemplate)
                .convertAndSend(eq(expectedDestination), eq(dto));

        // Act & Assert
        assertDoesNotThrow(() -> webSocketDataPushService.pushSpecificSensorData(sensorId, dto));

        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(dto));
    }
}