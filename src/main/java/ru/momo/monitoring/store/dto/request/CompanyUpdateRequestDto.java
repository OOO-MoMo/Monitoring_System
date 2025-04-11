package ru.momo.monitoring.store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Schema(description = "Запрос на обновление фирмы")
public record CompanyUpdateRequestDto(

        @NotNull(message = "UUID не должен быть null")
        UUID id,

        @Schema(description = "Название компании", example = "ООО Ромашка")
        String name,

        @Pattern(regexp = "\\d{12}", message = "ИНН должен содержать 12 цифр")
        @Schema(description = "ИНН компании (12 цифр)", example = "770708389333")
        String inn,

        @JsonProperty("head_office_address")
        @Schema(description = "Адрес головного офиса", example = "г. Москва, ул. Ленина, д. 1")
        String headOfficeAddress

) {
}
