package ru.momo.monitoring.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Структура ответа при возникновении ошибки")
public class ExceptionBody {

    @Schema(description = "Сообщение об ошибке", example = "Некорректный запрос")
    String message;

    @Schema(description = "Детализация ошибок валидации, где ключ — поле, а значение — сообщение ошибки",
            example = "{\"email\": \"Неверный формат email\", \"password\": \"Пароль должен содержать минимум 8 символов\"}")
    Map<String, String> errors;

    public ExceptionBody(String message) {
        this.message = message;
    }

}
