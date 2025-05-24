package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.enums.UserActionType;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(description = "Ответ с JWT-токенами и типом действия после регистрации пользователя")
public class RegisterJwtResponse {

    @Schema(description = "Уникальный идентификатор пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id;

    @Schema(description = "Имя пользователя (совпадает с его почтой)", example = "user@example.com")
    String username;

    @Schema(description = "Access-токен, используемый для авторизации",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken;

    @Schema(description = "Refresh-токен, используемый для обновления access-токена",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken;

    @Schema(description = "Тип действия регистрации")
    UserActionType actionType;
}