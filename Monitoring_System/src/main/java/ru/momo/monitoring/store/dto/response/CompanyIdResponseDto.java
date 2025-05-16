package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response dto id фирмы")
public record CompanyIdResponseDto(

        @Schema(description = "UUID компании", example = "11111111-1111-1111-1111-111111111111")
        UUID uuid

) {
}
