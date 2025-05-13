package ru.momo.monitoring.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TechnicPutDriverRequestDto(

        @Schema(description = "ID техники, которой назначается водитель", example = "f4e3aa68-efde-4f24-826c-28f2a87c3aa2")
        @NotNull(message = "Technic ID must not be null")
        UUID technicId,

        @Schema(description = "ID водителя, назначаемого на технику", example = "7fd1c201-4194-4aa9-9503-fd36c3ebc8ec")
        @NotNull(message = "Driver ID must not be null")
        UUID driverId

) {
}
