package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.services.AuthService;
import ru.momo.monitoring.store.dto.request.JwtRequest;
import ru.momo.monitoring.store.dto.request.RefreshJwtRequest;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.JwtResponse;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Аутентификация", description = "Методы для входа, регистрации, обновления токена и подтверждения email")
public class AuthController {

    final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Позволяет пользователю войти в систему, используя email и пароль. В ответе возвращает JWT-токены.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Неверный email или пароль",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь по переданному email не найден",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка сервера",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public JwtResponse login(@RequestBody @Validated JwtRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Обновление JWT-токена",
            description = "Позволяет обновить access-токен, используя refresh-токен.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Новый access-токен успешно сгенерирован",
                            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Недействительный или просроченный refresh-токен",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка сервера",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public JwtResponse refresh(@RequestBody RefreshJwtRequest refreshToken) {
        return authService.refresh(refreshToken);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Позволяет зарегистрировать нового пользователя. Только администратор или менеджер могут создавать учетные записи.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                            content = @Content(schema = @Schema(implementation = JwtResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав для выполнения операции",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public JwtResponse register(Principal principal, @RequestBody @Valid RegisterRequest request) {
        return authService.register(request, principal.getName());
    }

    @GetMapping("/confirm/{token}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Подтверждение email",
            description = "Позволяет подтвердить email-адрес пользователя, используя токен из письма.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email успешно подтвержден"),
                    @ApiResponse(responseCode = "400", description = "Token протух",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь по переданному email не найден",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка сервера",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public String confirmEmail(
            @Parameter(
                    name = "token",
                    description = "Токен для подтверждения email",
                    required = true,
                    in = ParameterIn.PATH)
            @PathVariable("token") String token
    ) {
        return authService.confirm(token);
    }

    @PostMapping("/resend-confirmation")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Повторная отправка письма с подтверждением email",
            description = "Позволяет повторно отправить письмо с подтверждением email, если пользователь не получил предыдущее.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Письмо успешно отправлено"),
                    @ApiResponse(responseCode = "400", description = "Email не указан или уже подтвержден",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь по переданному email не найден",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class))),
                    @ApiResponse(responseCode = "500", description = "Непредвиденная ошибка сервера",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public String resendConfirmationEmail(
            Principal principal) {

        return authService.resendConfirmationEmail(principal.getName());
    }


}
