package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicPutDriverRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicDataResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicPutDriverResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.entities.Technic;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/technic")
@Tag(name = "Техника", description = "API для управления техникой")
public class TechnicController {

    private final TechnicService technicService;

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Получение техники по ID",
            description = "Доступно только для администраторов.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные о технике успешно получены",
                            content = @Content(schema = @Schema(implementation = TechnicResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Техника не найдена",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public TechnicResponseDto getById(@PathVariable UUID id) {
        return technicService.getTechById(id);
    }

    @PostMapping("/")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Создание новой техники",
            description = "Доступно только для админов.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Новая единица техники успешно создана",
                            content = @Content(schema = @Schema(implementation = TechnicCreatedResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "409", description = "Неверные данные для новой единицы техники",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public TechnicCreatedResponseDto addNewTechnic(@RequestBody @Validated TechnicCreateRequestDto request) {
        return technicService.create(request);
    }

    @PutMapping("/driver")
    @PreAuthorize(value = "hasRole('ROLE_MANAGER')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Добавление водителя для техники",
            description = "Доступно только для менеджеров.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Действие выполнено успешно",
                            content = @Content(schema = @Schema(implementation = TechnicPutDriverResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь/техника не найдены",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public TechnicPutDriverResponseDto putNewDriver(@RequestBody @Validated TechnicPutDriverRequestDto request) {
        return technicService.putNewDriver(request);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(value = "hasRole('ROLE_MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Фильтрация техники",
            description = "Позволяет фильтровать список техники по компании, водителю, году выпуска, бренду, модели и активности. Доступно только для менеджеров.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список техники успешно получен",
                            content = @Content(schema = @Schema(implementation = Technic.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public List<TechnicResponseDto> searchTechnics(
            Principal principal,

            @Parameter(description = "ID владельца (водителя)", example = "11111111-1111-1111-1111-111111111111")
            @RequestParam(required = false) UUID ownerId,

            @Parameter(description = "Год выпуска техники", example = "2020")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Бренд техники", example = "JCB")
            @RequestParam(required = false) String brand,

            @Parameter(description = "Модель техники", example = "3CX")
            @RequestParam(required = false) String model,

            @Parameter(description = "Активна ли техника", example = "true")
            @RequestParam(required = false) Boolean isActive
    ) {
        return technicService.getFilteredTechnics(principal.getName(), ownerId, year, brand, model, isActive);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Получение всей техники по ID компании",
            description = "Возвращает список всей техники, принадлежащей указанной компании. Доступно только для администраторов.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список техники успешно получен",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TechnicResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Компания с указанным ID не найдена",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public List<TechnicResponseDto> getAllTechnicsByCompany(
            @Parameter(description = "ID компании, технику которой нужно получить", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable UUID companyId
    ) {
        return technicService.getAllTechnicsByCompanyId(companyId);
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Получение всей техники для авторизованного менеджера",
            description = "Возвращает список всей техники, принадлежащей компании, к которой относится менеджер. Доступно только для менеджеров.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список техники успешно получен",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TechnicResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Компания с указанным ID не найдена",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public List<TechnicResponseDto> getAllTechnicsForManager() {
        return technicService.getAllTechnicsForManager();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_DRIVER')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получение списка своей техники (для водителя)",
            description = """
                    Возвращает список техники, закрепленной за текущим аутентифицированным водителем. 
                    Доступно только для пользователей с ролью водителя.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список техники успешно получен",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TechnicResponseDto.class)))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав (пользователь не водитель или не активен)",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
            }
    )
    public List<TechnicResponseDto> getMyTechnics(Principal principal) {
        return technicService.getTechnicsForDriver(principal.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновление данных техники",
            description = """
                    Позволяет частично обновить информацию о существующей единице техники по ее ID.
                    Обновляются только те поля, которые переданы в теле запроса (не null).
                    Доступно для администраторов и менеджеров.
                    Поля `serialNumber`, `vin`, `ownerId`, `companyId` этим методом не изменяются.
                    Для смены водителя используйте PUT /api/v1/technic/driver.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Техника успешно обновлена",
                            content = @Content(schema = @Schema(implementation = TechnicResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные данные в запросе (ошибка валидации)",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Техника с указанным ID не найдена",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public TechnicResponseDto updateTechnic(
            @Parameter(description = "ID техники для обновления", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable UUID id,
            @RequestBody @Validated TechnicUpdateRequestDto request
    ) {
        return technicService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Удаление техники по ID",
            description = """
                    Полностью удаляет единицу техники из системы по ее уникальному идентификатору.
                    **Внимание:** Это необратимая операция.
                    Доступно только для пользователей с ролью администратора или менеджер.
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "Техника успешно удалена (нет содержимого в ответе)"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав для выполнения операции (пользователь не админ или не активен)",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Техника с указанным ID не найдена",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public ResponseEntity<Void> deleteTechnic(
            @Parameter(
                    description = "Уникальный идентификатор (UUID) техники, которую необходимо удалить",
                    required = true,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
            )
            @PathVariable UUID id
    ) {
        technicService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/data/{id}")
    public ResponseEntity<?> getSensorsData(@PathVariable UUID id) {
        TechnicDataResponseDto response = technicService.getSensorsData(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
