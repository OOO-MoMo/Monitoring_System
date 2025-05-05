package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Schema(description = "Данные для частичного обновления информации о технике. " +
        "Предоставляйте только те поля, которые нужно изменить.")
public record TechnicUpdateRequestDto(

        @Schema(description = "Новый бренд техники", example = "Caterpillar", maxLength = 255)
        @Length(max = 255, message = "Brand length must be smaller than 255 symbols")
        String brand,

        @Schema(description = "Новая модель техники", example = "D6", maxLength = 255)
        @Length(max = 255, message = "Model length must be smaller than 255 symbols")
        String model,

        @Schema(description = "Новый год выпуска техники (не ранее 1900)", example = "2021", minimum = "1900")
        @Min(value = 1900, message = "Year must be 1900 or later")
        Integer year,

        @Schema(description = "Новое описание техники", example = "Бульдозер в хорошем состоянии, после ТО", maxLength = 1000)
        @Length(max = 1000, message = "Description length must be smaller than 1000 symbols")
        String description,

        @Schema(description = "Новый статус активности техники (true - активна, false - неактивна)", example = "true")
        Boolean isActive,

        @Schema(description = "Новая дата последнего обслуживания (не может быть в будущем)",
                example = "2023-10-26T14:30:00", format = "date-time")
        @PastOrPresent(message = "Last service date cannot be in the future")
        LocalDateTime lastServiceDate,

        @Schema(description = "Новая дата следующего планового обслуживания",
                example = "2024-04-26T10:00:00", format = "date-time")
        LocalDateTime nextServiceDate

) {

}
