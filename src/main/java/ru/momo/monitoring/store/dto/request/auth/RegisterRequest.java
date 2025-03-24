package ru.momo.monitoring.store.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record RegisterRequest(

        @Email(message = "Ошибка в формате email`а")
        @Schema(description = "Email пользователя", example = "user@example.com")
        @JsonProperty("email")
        String email,

        @NotNull(message = "Пароль не должен быть пустым")
        @Schema(description = "Пароль пользователя", example = "securePassword123")
        @JsonProperty("password")
        String password,

        @JsonProperty(value = "password_confirmation")
        @NotNull(message = "Подтверждение пароля не должно быть пустым")
        @Schema(description = "Подтверждение пароля", example = "securePassword123")
        String passwordConfirmation

) {
}
