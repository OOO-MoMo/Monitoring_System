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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.ActiveDriversResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserRoleResponseDto;

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

    @PutMapping
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

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.OK)
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

    @GetMapping("/drivers/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Поиск активных водителей",
            description = "Позволяет искать водителей по фамилии, имени, отчеству или организации.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public ActiveDriversResponseDto searchActiveDrivers(
            @RequestParam(required = false) String firstname,
            @RequestParam(required = false) String lastname,
            @RequestParam(required = false) String patronymic,
            @RequestParam(required = false) String organization) {
        //return userService.searchActiveDrivers(firstname, lastname, patronymic, organization);
        throw new IllegalCallerException("Not implemented yet");
    }

}
