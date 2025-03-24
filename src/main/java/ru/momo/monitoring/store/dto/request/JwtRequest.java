package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Запрос для аутентификации пользователя")
public class JwtRequest {

    @NotNull(message = "Email must be not null")
    @Schema(description = "Email пользователя", example = "user@example.com")
    String email;

    @NotNull(message = "Password must be not null")
    @Schema(description = "Пароль пользователя", example = "securePassword123")
    String password;

}
