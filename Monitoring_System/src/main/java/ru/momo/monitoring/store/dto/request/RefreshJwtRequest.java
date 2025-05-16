package ru.momo.monitoring.store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление JWT-токена")
public class RefreshJwtRequest {

    @Schema(
            description = "Refresh-токен, используемый для обновления access-токена",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    @JsonProperty(value = "refresh_token")
    String refreshToken;

}
