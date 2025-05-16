package ru.momo.monitoring.store.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

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
        String passwordConfirmation,

        @NotNull(message = "UUID не должен быть null")
        @Schema(description = "UUID компании", example = "11111111-1111-1111-1111-111111111111")
        UUID companyId,

        @Size(max = 255, message = "Имя не должно превышать 255 символов")
        @Schema(description = "Имя пользователя", example = "Иван")
        String firstname,

        @Size(max = 255, message = "Фамилия не должна превышать 255 символов")
        @Schema(description = "Фамилия пользователя", example = "Иванов")
        String lastname,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Некорректный номер телефона")
        @Schema(description = "Номер телефона", example = "+79001234567")
        String phoneNumber

) {
}
