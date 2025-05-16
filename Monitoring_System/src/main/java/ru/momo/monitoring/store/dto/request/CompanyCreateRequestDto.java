package ru.momo.monitoring.store.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на создание компании")
public record CompanyCreateRequestDto(

        @NotNull
        @Schema(description = "Название компании", example = "ООО Ромашка")
        String name,

        @NotNull
        @Pattern(regexp = "\\d{12}", message = "ИНН должен содержать 12 цифр")
        @Schema(description = "ИНН компании (12 цифр)", example = "770708389333")
        String inn,

        @NotNull
        @JsonProperty("head_office_address")
        @Schema(description = "Адрес головного офиса", example = "г. Москва, ул. Ленина, д. 1")
        String headOfficeAddress

) {
}

