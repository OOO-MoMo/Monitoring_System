package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.CompanyIdResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserRoleResponseDto;
import ru.momo.monitoring.store.dto.response.UsersResponseDto;

import java.security.Principal;
import java.util.UUID;

@Tag(name = "Пользователи", description = "Методы для работы с пользователями")
@RequestMapping("/api/v1/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/current")
    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DRIVER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получение текущего пользователя",
            description = "Возвращает информацию о пользователе, который выполнил запрос.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные о пользователе успешно получены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UserResponseDto getCurrentUser(Principal principal) {
        return userService.getCurrentUserByEmail(principal.getName());
    }

    @GetMapping("/current/role")
    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DRIVER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Получение роли текущего пользователя",
            description = "Возвращает информацию о роли пользователе, который выполнил запрос.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные о пользователе успешно получены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UserRoleResponseDto getCurrentUserRole(Principal principal) {
        return userService.getCurrentUserRoleByEmail(principal.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Получение пользователя по ID",
            description = "Доступно только для администраторов.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные о пользователе успешно получены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UserResponseDto getById(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id) {
        return userService.getById(id);
    }

    @PostMapping
    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DRIVER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновление данных пользователя",
            description = "Позволяет пользователю обновить свои личные данные.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные успешно обновлены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UserResponseDto updateUser(
            Principal principal,
            @RequestBody @Validated UserUpdateRequestDto request) {
        return userService.update(request, principal.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Обновление данных пользователя по ID (для Администраторов/Менеджеров)",
            description = """
                    Позволяет Администратору или Менеджеру обновить личные данные указанного пользователя.
                    Менеджер может обновлять только пользователей внутри своей компании.
                    Обновляются только те поля, которые переданы в теле запроса (не null).
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные пользователя успешно обновлены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные в запросе (ошибка валидации, дубликат телефона)",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь (выполняющий запрос) не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав (не админ/менеджер, менеджер пытается обновить пользователя чужой компании, выполняющий пользователь не активен)",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь с указанным ID не найден",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UserResponseDto updateUserById(
            Principal principal,
            @Parameter(description = "UUID пользователя, которого нужно обновить", required = true)
            @PathVariable UUID id,
            @RequestBody @Validated UserUpdateRequestDto request
    ) {
        return userService.updateById(id, request, principal.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @CheckUserActive
    @Operation(
            summary = "Удаление пользователя",
            description = "Удаление пользователя по ID. Доступно только администраторам.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public void deleteUser(
            @Parameter(description = "UUID пользователя", required = true)
            @PathVariable UUID id) {
        userService.delete(id);
    }

    @GetMapping("/drivers/active/search")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Поиск активных водителей",
            description = """
                    Позволяет искать водителей по фамилии, имени, отчеству или организации. Метод предназначен для менеджеров.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UsersResponseDto searchActiveDrivers(
            Principal principal,

            @Parameter(description = "Имя водителя", example = "Иван")
            @RequestParam(required = false) String firstname,

            @Parameter(description = "Фамилия водителя", example = "Иванов")
            @RequestParam(required = false) String lastname,

            @Parameter(description = "Отчество водителя", example = "Иванович")
            @RequestParam(required = false) String patronymic
    ) {

        return userService.searchActiveDrivers(firstname, lastname, patronymic, principal.getName());
    }

    @GetMapping("/current/company")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @CheckUserActive
    @Operation(
            summary = "Возвращает id фирмы. Доступно только для менеджеров",
            description = "Возвращает id фирмы, к которой привязан менеджер. Доступно только для менеджеров",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены",
                            content = @Content(schema = @Schema(implementation = CompanyIdResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public CompanyIdResponseDto getCurrentCompany(Principal principal) {
        return userService.getCompanyIdForManager(principal.getName());
    }

    @GetMapping("/managers/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Поиск менеджеров.",
            description = """
                    Позволяет искать менеджеров по принадлежности к конкретной организации, имени, фамилии, отчеству.
                    Имеется фильтр на активных/неактивных пользователей. Метод предназначен для админов.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UsersResponseDto searchManagers(
            @Parameter(
                    description = "UUID фирмы, к которой относится менеджер",
                    example = "11111111-1111-1111-1111-111111111111"
            )
            @RequestParam UUID companyId,

            @Parameter(description = "Имя менеджера", example = "Иван")
            @RequestParam(required = false) String firstname,

            @Parameter(description = "Фамилия менеджера", example = "Иванов")
            @RequestParam(required = false) String lastname,

            @Parameter(description = "Отчество менеджера", example = "Иванович")
            @RequestParam(required = false) String patronymic,

            @Parameter(description = "Активна/неактивна запись менеджера", example = "True")
            @RequestParam(required = false, defaultValue = "True") Boolean isActive
    ) {
        return userService.searchManagers(
                companyId,
                firstname,
                lastname,
                patronymic,
                isActive
        );
    }

    @GetMapping("/drivers/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @CheckUserActive
    @Operation(
            summary = "Поиск водителей",
            description = """
                    Позволяет искать водителей по принадлежности к конкретной организации, имени, фамилии, отчеству.
                    Имеется фильтр на активных/неактивных пользователей. Метод предназначен для админов.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public UsersResponseDto searchDrivers(
            @Parameter(
                    description = "UUID фирмы, к которой относится водитель",
                    example = "11111111-1111-1111-1111-111111111111"
            )
            @RequestParam UUID companyId,

            @Parameter(description = "Имя водителя", example = "Иван")
            @RequestParam(required = false) String firstname,

            @Parameter(description = "Фамилия водителя", example = "Иванов")
            @RequestParam(required = false) String lastname,

            @Parameter(description = "Отчество водителя", example = "Иванович")
            @RequestParam(required = false) String patronymic,

            @Parameter(description = "Активна/неактивна запись водителя", example = "True")
            @RequestParam(required = false, defaultValue = "True") Boolean isActive
    ) {

        return userService.searchDrivers(
                companyId,
                firstname,
                lastname,
                patronymic,
                isActive
        );
    }

}
