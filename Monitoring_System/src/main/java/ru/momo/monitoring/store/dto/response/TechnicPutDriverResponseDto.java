package ru.momo.monitoring.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TechnicPutDriverResponseDto(

        @Schema(description = "ID техники, которой назначается водитель", example = "f4e3aa68-efde-4f24-826c-28f2a87c3aa2")
        UUID technicId,

        @Schema(description = "ID водителя, назначаемого на технику", example = "7fd1c201-4194-4aa9-9503-fd36c3ebc8ec")
        UUID driverId

) {
}
