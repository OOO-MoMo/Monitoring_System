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
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.request.SensorAssignmentRequest;
import ru.momo.monitoring.store.dto.request.UpdateSensorRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.dto.response.SensorsDto;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
@Tag(name = "Сенсоры", description = "API для управления сенсорами")
public class SensorController {

    private final SensorService sensorService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Регистрация нового сенсора",
            description = "Создание нового сенсора в системе. Доступно только администраторам с активным статусом"
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Привязать сенсор к технике",
            description = "Установка связи между сенсором и техникой. Доступно только администраторам компании"
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Отвязать сенсор от техники",
            description = "Удаление связи между сенсором и техникой. Доступно только администраторам компании"
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

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Получить все сенсоры указанной компании (для администратора)",
            description = "Возвращает список всех сенсоров, принадлежащих указанной компании. Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка сенсоров",
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
                    description = "Компания с указанным ID не найдена (если такая проверка реализована в сервисе)",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorsDto getAllSensorsByCompanyIdForAdmin(
            @Parameter(description = "ID компании", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable UUID companyId
    ) {
        return sensorService.getAllSensorsByCompanyIdForAdmin(companyId);
    }

    @GetMapping("/driver/sensors")
    @PreAuthorize("hasRole('ROLE_DRIVER')")
    @CheckUserActive
    @Operation(
            summary = "Получить все сенсоры на технике текущего водителя",
            description = "Возвращает список всех сенсоров, установленных на всей технике, назначенной текущему аутентифицированному водителю. Доступно только для пользователей с ролью 'DRIVER'."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка сенсоров",
                    content = @Content(schema = @Schema(implementation = SensorsDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен (например, пользователь не водитель или неактивен)",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь (водитель) не найден или не привязан к технике (возвращается пустой список)",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorsDto getSensorsForCurrentDriver() {
        return sensorService.getSensorsForDriver();
    }

    @GetMapping("/technic/{technicId}/sensors")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DRIVER')")
    @CheckUserActive
    @Operation(
            summary = "Получить сенсоры конкретной техники",
            description = "Возвращает список сенсоров для указанной техники. " +
                    "Администратор: доступ к сенсорам любой техники. " +
                    "Менеджер: доступ к сенсорам техники своей компании. " +
                    "Водитель: доступ к сенсорам своей назначенной техники (если это technicId его техники)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка сенсоров",
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
                    description = "Техника с указанным ID не найдена",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorsDto getSensorsByTechnicId(
            @Parameter(description = "ID техники", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable UUID technicId
    ) {
        return sensorService.getSensorsByTechnicId(technicId);
    }

    @PutMapping("/{sensorId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Обновить информацию о сенсоре",
            description = "Обновление данных существующего сенсора. Доступно только администраторам"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сенсор успешно обновлен",
                    content = @Content(schema = @Schema(implementation = SensorDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса или бизнес-логики (например, дубликат серийного номера)",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
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
                    description = "Сенсор или связанный тип сенсора не найден",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт данных (например, серийный номер уже занят другим сенсором)",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorDto updateSensor(
            @Parameter(
                    description = "ID сенсора для обновления",
                    required = true,
                    example = "0c3a85a6-2982-4997-93cd-16384d8f6a21"
            )
            @PathVariable UUID sensorId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления сенсора (все поля опциональны)",
                    required = true
            )
            @Valid @RequestBody UpdateSensorRequest request
    ) {
        return sensorService.updateSensor(sensorId, request);
    }

    @DeleteMapping("/{sensorId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Удалить сенсор",
            description = "Удаление сенсора из системы. Доступно только администраторам. Сенсор не должен быть привязан к технике."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Сенсор успешно удален"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невозможно удалить сенсор (например, он привязан к технике)",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен (например, администратор другой компании)",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сенсор не найден",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSensor(
            @Parameter(
                    description = "ID сенсора для удаления",
                    required = true,
                    example = "0c3a85a6-2982-4997-93cd-16384d8f6a21"
            )
            @PathVariable UUID sensorId
    ) {
        sensorService.deleteSensor(sensorId);
    }

}
