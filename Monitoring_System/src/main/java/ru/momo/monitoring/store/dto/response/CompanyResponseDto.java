package ru.momo.monitoring.store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.momo.monitoring.store.entities.Company;

import java.util.UUID;

@Schema(description = "Response dto фирмы")
public record CompanyResponseDto(

        @Schema(description = "UUID компании", example = "11111111-1111-1111-1111-111111111111")
        UUID uuid,

        @Schema(description = "Название компании", example = "ООО Ромашка")
        String name,

        @Schema(description = "ИНН компании (12 цифр)", example = "770708389333")
        String inn,

        @JsonProperty("head_office_address")
        @Schema(description = "Адрес головного офиса", example = "г. Москва, ул. Ленина, д. 1")
        String headOfficeAddress

) {
    public static CompanyResponseDto mapFromEntity(Company company) {
        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getInn(),
                company.getHeadOfficeAddress()
        );
    }
}
