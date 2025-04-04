package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.momo.monitoring.store.entities.Technic;

import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TechnicCreatedResponseDto {

    @Schema(description = "Уникальный идентификатор техники", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id;

    @Schema(description = "Модель техники", example = "X-2000", requiredMode = Schema.RequiredMode.REQUIRED)
    String model;

    @Schema(description = "Производитель техники", example = "TechCorp", requiredMode = Schema.RequiredMode.REQUIRED)
    String brand;

    @Schema(description = "Год выпуска", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer year;

    @Schema(description = "Серийный номер", example = "SN-987654321")
    String serialNumber;

    @Schema(description = "Идентификационный номер (VIN)", example = "1ABCD23EFGH456789")
    String vin;

    @Schema(description = "Дополнительное описание или комментарии", example = "В отличном состоянии, регулярное обслуживание")
    String description;

    public static TechnicCreatedResponseDto MapFromEntity(Technic technic) {
        return TechnicCreatedResponseDto
                .builder()
                .id(technic.getId())
                .model(technic.getModel())
                .brand(technic.getBrand())
                .year(technic.getYear())
                .serialNumber(technic.getSerialNumber())
                .vin(technic.getVin())
                .description(technic.getDescription())
                .build();
    }

}
