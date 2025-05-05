package ru.momo.monitoring.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Стандартная структура ответа при ошибке")
public class ExceptionBody {

    @Schema(description = "Время возникновения ошибки", example = "2023-10-27T10:15:30")
    LocalDateTime timestamp;

    @Schema(description = "HTTP статус код", example = "404")
    int status;

    @Schema(description = "Краткое описание статуса HTTP", example = "Not Found")
    String error;

    @Schema(description = "Уникальный код ошибки для программной обработки", example = "RESOURCE_NOT_FOUND")
    String errorCode;

    @Schema(description = "Сообщение об ошибке, понятное пользователю или фронтенд-разработчику", example = "Техника с указанным ID не найдена")
    String message;

    @Schema(description = "Детализация ошибок валидации (ключ - поле, значение - сообщение)",
            example = "{\"email\": \"Неверный формат email\", \"password\": \"Пароль должен содержать минимум 8 символов\"}")
    Map<String, String> errors;

    public ExceptionBody(HttpStatus httpStatus, String errorCode, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.errorCode = errorCode;
        this.message = message;
    }

    public ExceptionBody(HttpStatus httpStatus, String errorCode, String message, Map<String, String> errors) {
        this(httpStatus, errorCode, message);
        this.errors = errors;
    }
}
