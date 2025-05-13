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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.annotations.CheckUserActive;
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.services.SensorTypeService;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorTypeRequest;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sensor-types")
@RequiredArgsConstructor
@Tag(name = "Типы сенсоров", description = "Управление типами сенсоров")
public class SensorTypeController {

    private final SensorTypeService sensorTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создание нового типа сенсора",
            description = "Доступно только администраторам с активным статусом"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Тип успешно создан",
                    content = @Content(schema = @Schema(implementation = SensorTypeDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Тип с таким именем уже существует",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorTypeDto createSensorType(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания типа",
                    required = true
            )
            @Valid @RequestBody CreateSensorTypeRequest request) {
        return sensorTypeService.createSensorType(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получение типа сенсора по ID",
            description = "Возвращает полные данные о типе. Доступно только администраторам/менеджерам с активным статусом."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение данных",
                    content = @Content(schema = @Schema(implementation = SensorTypeDto.class))
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
                    description = "Тип не найден",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorTypeDto getSensorType(
            @Parameter(description = "UUID типа сенсора", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return sensorTypeService.getSensorTypeById(id);
    }

    @GetMapping
    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получение всех типов сенсоров",
            description = "Возвращает список всех зарегистрированных типов. Доступно только администраторам/менеджерам с активным статусом"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка",
                    content = @Content(schema = @Schema(implementation = SensorTypesDto.class))
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
            )
    })
    public SensorTypesDto getAllSensorTypes() {
        return sensorTypeService.getAllSensorTypes();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновление существующего типа сенсора",
            description = "Обновляет данные типа сенсора по ID. Доступно только администраторам с активным статусом."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Тип успешно обновлен",
                    content = @Content(schema = @Schema(implementation = SensorTypeDto.class))
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
                    description = "Доступ запрещен / Пользователь не активен",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Тип для обновления не найден",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Тип с таким новым именем уже существует",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorTypeDto updateSensorType(
            @Parameter(description = "UUID типа сенсора для обновления", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Новые данные для типа", required = true)
            @Valid @RequestBody UpdateSensorTypeRequest request) {
        return sensorTypeService.updateSensorType(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удаление типа сенсора",
            description = "Удаляет тип сенсора по ID. Доступно только администраторам с активным статусом."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Тип успешно удален"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен / Пользователь не активен",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Тип для удаления не найден",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public void deleteSensorType(
            @Parameter(description = "UUID типа сенсора для удаления", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        sensorTypeService.deleteSensorType(id);
    }

}