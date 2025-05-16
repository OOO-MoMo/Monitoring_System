package ru.momo.monitoring.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.momo.monitoring.store.entities.enums.SensorStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для передачи данных сенсора в реальном времени (например, через WebSocket)")
public class SensorDataRealtimeDto {

    @Schema(description = "Уникальный идентификатор сенсора", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID sensorId;

    @Schema(description = "Уникальный идентификатор техники, к которой привязан сенсор (может быть null)", example = "b2c3d4e5-f6a7-8901-2345-67890abcdef0")
    private UUID technicId;

    @Schema(description = "Серийный номер сенсора", example = "SN-12345XYZ")
    private String sensorSerialNumber;

    @Schema(description = "Текущее значение сенсора (в виде строки)", example = "25.77")
    private String value;

    @Schema(description = "Временная метка получения значения", example = "2024-05-14T10:30:55.123456")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    @Schema(description = "Статус значения сенсора")
    private SensorStatus status;

    @Schema(description = "Тип сенсора (например, TEMPERATURE, HUMIDITY)", example = "TEMPERATURE")
    private String sensorType;

    @Schema(description = "Единицы измерения значения (например, °C, %, ppm)", example = "°C")
    private String unitOfMeasurement;
}