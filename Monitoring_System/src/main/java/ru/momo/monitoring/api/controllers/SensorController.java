package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.annotations.CheckUserActive;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.services.ExcelExportService;
import ru.momo.monitoring.services.SecurityService;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.store.dto.request.CreateSensorRequest;
import ru.momo.monitoring.store.dto.request.SensorAssignmentRequest;
import ru.momo.monitoring.store.dto.request.SensorDataHistoryDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorRequest;
import ru.momo.monitoring.store.dto.response.SensorDto;
import ru.momo.monitoring.store.dto.response.SensorsDto;
import ru.momo.monitoring.store.entities.Sensor;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.AggregationType;
import ru.momo.monitoring.store.entities.enums.DataGranularity;
import ru.momo.monitoring.store.entities.enums.RoleName;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
@Tag(name = "Сенсоры", description = "API для управления сенсорами")
public class SensorController {

    private final SensorService sensorService;

    private final ExcelExportService excelExportService;

    private final SecurityService securityService;

    private static final DateTimeFormatter REPORT_HEADER_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static final DateTimeFormatter FILENAME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания сенсора",
                    required = true
            )
            @Valid @RequestBody CreateSensorRequest request
    ) {
        return sensorService.registerSensor(request);
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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для привязки сенсора",
                    required = true
            )
            @Valid @RequestBody SensorAssignmentRequest request
    ) {
        sensorService.assignToTechnic(request);
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
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для отвязки сенсора",
                    required = true
            )
            @Valid @RequestBody SensorAssignmentRequest request
    ) {
        sensorService.unassignFromTechnic(request);
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

    @GetMapping("/sensorType/{sensorTypeId}/sensors")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Получить сенсоры конкретного типа",
            description = "Возвращает список сенсоров для указанного типа сенсоров. " +
                    "Администратор: доступ есть."
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
                    description = "Типа сенсора с указанным ID не найден",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))
            )
    })
    public SensorsDto getSensorsBySensorTypeId(
            @Parameter(description = "ID типа сенсора", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable UUID sensorTypeId
    ) {
        return sensorService.getSensorsBySensorTypeId(sensorTypeId);
    }

    @GetMapping("/all-paged")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Получить все сенсоры с пагинацией и фильтрацией (для администратора)",
            description = "Возвращает страницу со списком всех сенсоров в системе. " +
                    "Можно фильтровать по признаку привязки к технике. " +
                    "Доступно только администраторам."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение страницы сенсоров",
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
            )
    })
    public SensorsDto getAllSensorsPaged(
            @Parameter(description = "Фильтр по привязке к технике: " +
                    "true - только привязанные, " +
                    "false - только не привязанные, " +
                    "не указан - все сенсоры.",
                    required = false, example = "true")
            @RequestParam(required = false) Boolean attachedToTechnic,

            @PageableDefault(size = 20, sort = "serialNumber")
            @Parameter(description = "Параметры пагинации и сортировки (page, size, sort). " +
                    "Пример sort: serialNumber,asc или serialNumber,desc. " +
                    "Можно указывать несколько полей для сортировки: sort=serialNumber,asc&sort=manufacturer,desc")
            Pageable pageable
    ) {
        return sensorService.getAllSensorsPaged(attachedToTechnic, pageable);
    }

    @GetMapping("/{sensorId}/history")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DRIVER')")
    @CheckUserActive
    @Operation(
            summary = "Получить историю данных сенсора",
            description = """
                    Возвращает историю показаний для указанного сенсора за заданный временной период.
                    Поддерживается получение сырых данных или агрегированных данных по различным временным интервалам.
                    Все временные метки (`from`, `to` и в ответе) обрабатываются и предполагаются в **UTC**.
                    Клиент должен передавать параметры `from` и `to` в формате ISO 8601 (например, YYYY-MM-DDTHH:MM:SS).
                    Если часовой пояс не указан явно (например, суффиксом 'Z'), время будет интерпретировано согласно настройкам сервера,
                    поэтому рекомендуется всегда передавать время в UTC с указанием 'Z' или без смещения, если сервер по умолчанию работает в UTC.
                    Администраторы могут использовать данный метод с любыми сенсорами, менеджеры только с сенсорами своей компании.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "История данных успешно получена. Возвращает массив объектов `SensorDataHistoryDto`.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SensorDataHistoryDto.class)))
            ),
            @ApiResponse(responseCode = "400", description = """
                    Некорректные параметры запроса. Возможные причины:
                    - Параметр `from` указан позже, чем `to`.
                    - Неверный формат даты/времени для `from` или `to`.
                    - Недопустимое значение для `granularity` или `aggregationType`.
                    - `aggregationType` указан без `granularity` (кроме RAW).
                    """,
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен. Пользователь авторизован, но не имеет прав на просмотр истории этого сенсора или на выполнение операции.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Сенсор с указанным `sensorId` не найден.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
    })
    public List<SensorDataHistoryDto> getSensorHistory(
            @Parameter(
                    name = "sensorId",
                    description = "Уникальный идентификатор (UUID) сенсора, для которого запрашивается история.",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
            )
            @PathVariable UUID sensorId,

            @Parameter(
                    name = "from",
                    description = "Начало временного периода для выборки данных. Формат ISO 8601 (YYYY-MM-DDTHH:MM:SS). Рекомендуется передавать в UTC (например, с суффиксом 'Z').",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "2024-05-01T00:00:00"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(
                    name = "to",
                    description = "Конец временного периода для выборки данных. Формат ISO 8601 (YYYY-MM-DDTHH:MM:SS). Рекомендуется передавать в UTC (например, с суффиксом 'Z').",
                    required = true,
                    in = ParameterIn.QUERY,
                    example = "2024-05-14T23:59:59" // или "2024-05-14T23:59:59Z"
            )
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(
                    name = "granularity",
                    description = """
                            Гранулярность агрегации данных. Определяет временной интервал, по которому будут сгруппированы данные.
                            Если параметр не указан или равен `RAW`, возвращаются исходные (сырые) данные без агрегации.
                            """,
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(implementation = DataGranularity.class, defaultValue = "RAW",
                            description = "Допустимые значения: RAW, SECOND, MINUTE, HOUR, DAY.")
            )
            @RequestParam(required = false) DataGranularity granularity,

            @Parameter(
                    name = "aggregationType",
                    description = """
                            Тип агрегации, применяемый к данным внутри каждого интервала гранулярности.
                            Этот параметр **игнорируется**, если `granularity` не указан или равен `RAW`.
                            Если `granularity` указан (например, HOUR), а `aggregationType` нет,
                             по умолчанию может применяться `AVG` (среднее),
                            либо сервер может вернуть ошибку, требуя явного указания типа агрегации 
                            (зависит от реализации).
                            Для агрегаций `FIRST` и `LAST` также возвращается исходный статус записи, если он был.
                            Для `AVG`, `MIN`, `MAX`, `SUM`, `COUNT` поле `status` в ответе обычно будет `null` 
                            или `UNDEFINED`.
                            """,
                    required = false,
                    in = ParameterIn.QUERY,
                    schema = @Schema(implementation = AggregationType.class,
                            description = "Допустимые значения: AVG, MIN, MAX, SUM, COUNT, FIRST, LAST.")
            )
            @RequestParam(required = false) AggregationType aggregationType
    ) {
        return sensorService.getSensorDataHistory(sensorId, from, to, granularity, aggregationType);
    }

    @GetMapping("/{sensorId}/history/export/excel")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Экспорт истории данных сенсора в Excel",
            description = "Генерирует и возвращает Excel файл (.xlsx) с историей показаний сенсора за указанный период. " +
                    "Параметры `from`, `to`, `granularity`, `aggregationType` работают так же, как для эндпоинта `/history`."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Excel файл успешно сформирован и возвращен.",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "404", description = "Сенсор не найден.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера при генерации отчета.",
                    content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
    })
    public ResponseEntity<InputStreamResource> exportSensorHistoryToExcel(
            @Parameter(description = "ID сенсора", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable UUID sensorId,

            @Parameter(description = "Начало периода (ISO 8601 UTC)", required = true, example = "2024-05-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Конец периода (ISO 8601 UTC)", required = true, example = "2024-05-14T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(description = "Гранулярность агрегации", required = false, schema = @Schema(implementation = DataGranularity.class, defaultValue = "RAW"))
            @RequestParam(required = false) DataGranularity granularity,

            @Parameter(description = "Тип агрегации", required = false, schema = @Schema(implementation = AggregationType.class))
            @RequestParam(required = false) AggregationType aggregationType
    ) {
        User currentUser = securityService.getCurrentUser();

        boolean hasAccess = false;

        if (currentUser.getRole() == RoleName.ROLE_ADMIN) {
            hasAccess = true;
        } else {
            Sensor sensor = sensorService.getSensorEntityById(sensorId);

            if (sensor == null) {
                throw new ResourceNotFoundException("Sensor with id " + sensorId + " not found for access check.");
            }

            if (currentUser.getRole() == RoleName.ROLE_MANAGER) {
                if (currentUser.getCompany() != null && sensor.getCompany() != null &&
                        currentUser.getCompany().getId().equals(sensor.getCompany().getId())) {
                    hasAccess = true;
                }
            } else if (currentUser.getRole() == RoleName.ROLE_DRIVER) {
                Technic technicOfSensor = sensor.getTechnic();
                if (technicOfSensor != null && technicOfSensor.getOwnerId() != null &&
                        technicOfSensor.getOwnerId().getId().equals(currentUser.getId())) {
                    hasAccess = true;
                }
            }
        }

        if (!hasAccess) {
            log.warn("Access denied for user {} (Role: {}) to sensor {}",
                    currentUser.getEmail(), currentUser.getRole(), sensorId);
            throw new AccessDeniedException("User does not have permission to access history for this sensor.");
        }


        List<SensorDataHistoryDto> historyData = sensorService.getSensorDataHistory(sensorId, from, to, granularity, aggregationType);

        String sensorInfoForReport;
        String sensorSerialNumberForFilename = "sensor_" + sensorId.toString().substring(0, 8);
        try {
            ru.momo.monitoring.store.entities.Sensor sensorEntity = sensorService.getSensorEntityById(sensorId);
            SensorType type = sensorEntity.getType();
            String typeName = (type != null && type.getName() != null) ? type.getName() : "N/A";
            String unit = (type != null && type.getUnit() != null) ? " (" + type.getUnit() + ")" : "";
            sensorSerialNumberForFilename = sensorEntity.getSerialNumber() != null ? sensorEntity.getSerialNumber() : sensorSerialNumberForFilename;
            sensorInfoForReport = String.format("%s (S/N: %s)%s", typeName, sensorSerialNumberForFilename, unit);

        } catch (ResourceNotFoundException e) {
            log.warn("Sensor with id {} not found for Excel report header, using default info.", sensorId);
            sensorInfoForReport = "Сенсор ID: " + sensorId.toString();
        } catch (Exception e) {
            log.error("Error fetching sensor details for Excel report, sensorId: {}: {}", sensorId, e.getMessage());
            sensorInfoForReport = "Сенсор ID: " + sensorId.toString() + " (детали не загружены)";
        }

        ByteArrayInputStream excelFileStream = excelExportService.generateSensorHistoryExcel(
                sensorId,
                sensorInfoForReport,
                from.format(REPORT_HEADER_DATE_TIME_FORMATTER),
                to.format(REPORT_HEADER_DATE_TIME_FORMATTER),
                LocalDateTime.now().format(REPORT_HEADER_DATE_TIME_FORMATTER),
                historyData
        );

        HttpHeaders headers = new HttpHeaders();
        String cleanSensorName = sensorSerialNumberForFilename.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
        String filename = String.format("%s_history_%s_to_%s.xlsx",
                cleanSensorName,
                from.format(FILENAME_DATE_FORMATTER),
                to.format(FILENAME_DATE_FORMATTER));
        headers.add("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelFileStream));
    }

}
