package ru.momo.monitoring.store.dto.request;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Элемент истории данных сенсора. Для агрегированных данных 'status' может отсутствовать или быть неприменим.")
public class SensorDataHistoryDto {

    @Schema(
            description = "Временная метка значения (или начала интервала агрегации). Предполагается UTC.",
            example = "2024-05-14T10:30:00"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    @Schema(description = """
            Значение сенсора (или агрегированное значение). Может быть null,
             если нет данных за интервал или значение нечисловое.", example = "25.77
            """)
    private Double value;

    @Schema(
            description = """
                    Статус значения сенсора. Для агрегированных данных (AVG, MIN, MAX, SUM, COUNT)
                     это поле обычно не имеет смысла и может быть null.
                    Для FIRST/LAST можно передавать статус исходной записи.
                    """,
            nullable = true)
    private SensorStatus status;
}