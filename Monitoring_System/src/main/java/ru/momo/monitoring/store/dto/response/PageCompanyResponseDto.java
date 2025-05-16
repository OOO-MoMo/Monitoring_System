package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ответ со списком компаний и данными пагинации")
public record PageCompanyResponseDto(

        @ArraySchema(
                schema = @Schema(implementation = CompanyResponseDto.class),
                arraySchema = @Schema(description = "Список компаний")
        )
        List<CompanyResponseDto> content,

        @Schema(description = "Текущая страница", example = "0")
        int page,

        @Schema(description = "Размер страницы", example = "10")
        int size,

        @Schema(description = "Общее количество элементов", example = "42")
        long totalElements,

        @Schema(description = "Общее количество страниц", example = "5")
        int totalPages

) {
}
