package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import ru.momo.monitoring.services.CompanyService;
import ru.momo.monitoring.store.dto.request.CompanyCreateRequestDto;
import ru.momo.monitoring.store.dto.request.CompanyUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.CompanyResponseDto;
import ru.momo.monitoring.store.dto.response.PageCompanyResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;

import java.util.UUID;

@Tag(name = "Фирмы", description = "Методы для работы с фирмами")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Регистрация новой фирмы",
            description = "Доступно только для админов.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Новая фирма успешно зарегистрирована",
                            content = @Content(schema = @Schema(implementation = CompanyResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "409", description = "Неверные данные для новой фирмы",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public CompanyResponseDto addNewCompany(@RequestBody @Validated CompanyCreateRequestDto request) {
        return companyService.create(request);
    }

    @GetMapping
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Получение списка компаний",
            description = "Доступно для админа. Позволяет получить список всех компаний с фильтрацией по названию и пагинацией.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Компании успешно получены",
                            content = @Content(schema = @Schema(implementation = PageCompanyResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public PageCompanyResponseDto findAllCompanies(
            @RequestParam(required = false) @Parameter(description = "Фильтрация по названию") String name,
            @ParameterObject Pageable pageable
    ) {
        return companyService.findAll(name, pageable);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Удаление фирмы",
            description = "Удаление фирмы по ID. Доступно только администраторам.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Фирма успешно удалена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Фирма не найдена",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public void deleteCompany(
            @Parameter(description = "UUID фирмы", required = true)
            @PathVariable UUID id) {
        companyService.delete(id);
    }

    @PutMapping
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Обновление данных фирмы",
            description = "Позволяет админу обновить данные фирмы.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные успешно обновлены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public CompanyResponseDto updateCompany(
            @RequestBody @Validated CompanyUpdateRequestDto request) {
        return companyService.update(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получение фирмы по ID",
            description = "Возвращает детали конкретной фирмы по ее UUID. Доступно только для администраторов.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные фирмы успешно получены",
                            content = @Content(schema = @Schema(implementation = CompanyResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Фирма с указанным ID не найдена",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public CompanyResponseDto getCompanyById(
            @Parameter(description = "UUID фирмы", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable UUID id
    ) {
        return companyService.getCompanyById(id);
    }

}
