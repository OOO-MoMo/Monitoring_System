package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.annotations.CheckUserActive;
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.request.SensorAssignmentRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.dto.response.SensorsDto;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
@Tag(name = "Сенсоры", description = "API для управления сенсорами")
public class SensorController {

    private final SensorService sensorService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Регистрация нового сенсора",
            description = "Создание нового сенсора в системе. Доступно только менеджерам с активным статусом"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Сенсор успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = SensorDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Сенсор с таким серийным номером уже существует",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorDto registerSensor(
            @Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания сенсора",
                    required = true
            )
            @Valid @RequestBody CreateSensorRequest request
    ) {
        return sensorService.registerSensor(request, principal.getName());
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Привязать сенсор к технике",
            description = "Установка связи между сенсором и техникой. Доступно только менеджерам компании"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сенсор успешно привязан"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сенсор или техника не найдены",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Сенсор уже привязан к этой технике",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public void assignToTechnic(
            @Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для привязки сенсора",
                    required = true
            )
            @Valid @RequestBody SensorAssignmentRequest request
    ) {
        sensorService.assignToTechnic(request, principal.getName());
    }

    @PostMapping("/unassign")
    @PreAuthorize("hasRole('MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Отвязать сенсор от техники",
            description = "Удаление связи между сенсором и техникой. Доступно только менеджерам компании"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сенсор успешно отвязан"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сенсор или техника не найдены",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Сенсор не привязан к указанной технике",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public void unassignFromTechnic(
            @Parameter(hidden = true) Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для отвязки сенсора",
                    required = true
            )
            @Valid @RequestBody SensorAssignmentRequest request
    ) {
        sensorService.unassignFromTechnic(request, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Получить все сенсоры компании",
            description = "Возвращает список всех сенсоров, принадлежащих компании менеджера"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка",
                    content = @Content(schema = @Schema(implementation = SensorsDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Компания не найдена",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorsDto getAllCompanySensors(
            @Parameter(hidden = true) Principal principal
    ) {
        return sensorService.getAllCompanySensors(principal.getName());
    }
}