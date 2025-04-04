package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.momo.monitoring.store.entities.Technic;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "DTO для создания новой записи о технике")
public class TechnicCreateRequestDto {

    @NotNull(message = "Model must be not null")
    @Length(max = 255, message = "Model length must be smaller than 255 symbols")
    @Schema(description = "Модель техники", example = "X-2000", requiredMode = Schema.RequiredMode.REQUIRED)
    String model;

    @NotNull(message = "Brand must be not null")
    @Length(max = 255, message = "Brand length must be smaller than 255 symbols")
    @Schema(description = "Производитель техники", example = "TechCorp", requiredMode = Schema.RequiredMode.REQUIRED)
    String brand;

    @Min(value = 1900, message = "Year must be a valid year")
    @Schema(description = "Год выпуска", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer year;

    @Length(max = 255, message = "Serial number length must be smaller than 255 symbols")
    @Schema(description = "Серийный номер", example = "SN-987654321")
    String serialNumber;

    @Length(max = 255, message = "VIN length must be smaller than 255 symbols")
    @Schema(description = "Идентификационный номер (VIN)", example = "1ABCD23EFGH456789")
    String vin;

    @Schema(description = "Дополнительное описание или комментарии", example = "В отличном состоянии, регулярное обслуживание")
    String description;

    public static Technic mapToTechnicEntity(TechnicCreateRequestDto dto) {
        return Technic.builder()
                .model(dto.getModel())
                .brand(dto.getBrand())
                .year(dto.getYear())
                .serialNumber(dto.getSerialNumber())
                .vin(dto.getVin())
                .description(dto.getDescription())
                .build();
    }
}

