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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.exceptions.UserBadRequestException;
import ru.momo.monitoring.services.AuthService;
import ru.momo.monitoring.store.dto.request.JwtRequest;
import ru.momo.monitoring.store.dto.request.RefreshJwtRequest;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.JwtResponse;
import ru.momo.monitoring.store.dto.response.RegisterJwtResponse;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Аутентификация", description = "Методы для входа, регистрации, обновления токена и подтверждения email")
public class AuthController {

    final AuthService authService;

    @Value("${application.frontend.url}")
    String frontendUrl;

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
                            content = @Content(schema = @Schema(implementation = RegisterJwtResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав для выполнения операции",
                            content = @Content(schema = @Schema(implementation = ExceptionBody.class)))
            }
    )
    public RegisterJwtResponse register(Principal principal, @RequestBody @Valid RegisterRequest request) {
        return authService.register(request, principal.getName());
    }

    @GetMapping(value = "/confirm/{token}")
    @Operation(
            summary = "Подтверждение email",
            description = "Позволяет подтвердить email-адрес пользователя, используя токен из письма. В случае успеха отображает страницу подтверждения.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email успешно подтвержден (возвращается HTML страница).",
                            content = @Content(mediaType = MediaType.TEXT_HTML_VALUE)),
                    @ApiResponse(responseCode = "400", description = "Токен недействителен или истек (возвращается HTML страница с ошибкой).",
                            content = @Content(mediaType = MediaType.TEXT_HTML_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Пользователь по email из токена не найден (возвращается HTML страница с ошибкой).",
                            content = @Content(mediaType = MediaType.TEXT_HTML_VALUE)),
            }
    )
    public ResponseEntity<String> confirmEmail(
            @Parameter(
                    name = "token",
                    description = "Токен для подтверждения email",
                    required = true,
                    in = ParameterIn.PATH)
            @PathVariable("token") String token
    ) {
        String title;
        String message;
        String buttonText = "Перейти на сайт";
        String buttonLink = frontendUrl;
        boolean success = false;

        try {
            authService.confirm(token);
            title = "Email успешно подтвержден!";
            message = "Ваш адрес электронной почты был успешно подтвержден. Теперь вы можете войти в систему, используя свои учетные данные.";
            success = true;
        } catch (UserBadRequestException e) {
            log.warn("Confirmation failed for token {}: {}", token, e.getMessage());
            title = "Ошибка подтверждения Email";
            message = "Ссылка для подтверждения недействительна или срок ее действия истек. Пожалуйста, запросите новую ссылку для подтверждения.";
            buttonText = "Запросить новую ссылку";
        } catch (ResourceNotFoundException e) {
            log.warn("Confirmation failed: User not found for token {}.", token);
            title = "Ошибка подтверждения Email";
            message = "Не удалось найти пользователя, связанного с этой ссылкой подтверждения.";
        }

        String htmlPage = buildConfirmationHtmlPage(title, message, buttonText, buttonLink, success);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlPage);
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

    private String buildConfirmationHtmlPage(String title, String message, String buttonText, String buttonLink, boolean success) {
        String backgroundColor = success ? "#e9f7ef" : "#fdecea";
        String titleColor = success ? "#28a745" : "#dc3545";
        String buttonColor = success ? "#28a745" : "#007bff";

        return String.format("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body { display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; background-color: %s; font-family: Arial, sans-serif; text-align: center; padding: 20px; box-sizing: border-box; }
                        .container { background-color: #fff; padding: 30px 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); max-width: 500px; width: 100%%; }
                        h1 { color: %s; font-size: 24px; margin-bottom: 15px; }
                        p { color: #555; font-size: 16px; line-height: 1.6; margin-bottom: 25px; }
                        .button { display: inline-block; padding: 12px 25px; background-color: %s; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px; transition: background-color 0.2s ease; }
                        .button:hover { opacity: 0.9; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>%s</h1>
                        <p>%s</p>
                        <a href="%s" class="button">%s</a>
                    </div>
                </body>
                </html>
                """, title, backgroundColor, titleColor, buttonColor, title, message, buttonLink, buttonText);
    }

}
