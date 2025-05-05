package ru.momo.monitoring.api.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.ErrorCodes;
import ru.momo.monitoring.exceptions.ExceptionBody;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.exceptions.SensorNotCreatedException;
import ru.momo.monitoring.exceptions.UserBadRequestException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionBody> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                e.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserBadRequestException.class)
    public ResponseEntity<ExceptionBody> handleUserBadRequestException(UserBadRequestException e) {
        log.warn("User bad request: {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.USER_BAD_REQUEST,
                e.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SensorBadRequestException.class)
    public ResponseEntity<ExceptionBody> handleSensorBadRequestException(SensorBadRequestException e) {
        log.warn("Sensor bad request: {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.SENSOR_BAD_REQUEST,
                e.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityDuplicationException.class)
    public ResponseEntity<ExceptionBody> handleEntityDuplicationException(EntityDuplicationException e) {
        log.warn("Entity duplication: {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.CONFLICT,
                ErrorCodes.DUPLICATE_ENTITY,
                e.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionBody> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
                ));
        ExceptionBody body = new ExceptionBody(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                "Ошибка валидации введенных данных",
                errors
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionBody> handleAuthentication(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.UNAUTHORIZED,
                ErrorCodes.AUTHENTICATION_FAILURE,
                "Ошибка аутентификации. Проверьте логин и пароль."
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionBody> handleCustomAccessDenied(AccessDeniedException e) {
        log.warn("Access denied (custom): {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.FORBIDDEN,
                ErrorCodes.ACCESS_DENIED,
                "Доступ запрещен."
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ExceptionBody> handleAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        log.warn("Access denied (Spring Security): {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.FORBIDDEN,
                ErrorCodes.ACCESS_DENIED,
                "Доступ запрещен."
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionBody> handleExpiredJwtException(ExpiredJwtException e) {
        log.warn("Token has expired: {}", e.getMessage());
        ExceptionBody body = new ExceptionBody(
                HttpStatus.UNAUTHORIZED,
                ErrorCodes.TOKEN_EXPIRED,
                "Срок действия сессии истек. Пожалуйста, войдите снова."
        );
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SensorNotCreatedException.class)
    public ResponseEntity<ExceptionBody> handleSensorNotCreatedException(SensorNotCreatedException e) {
        log.error("Sensor creation failed: {}", e.getMessage(), e);
        ExceptionBody body = new ExceptionBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.SENSOR_CREATION_FAILED,
                "Не удалось создать сенсор. Попробуйте позже."
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionBody> handleIllegalState(IllegalStateException e) {
        log.error("Illegal application state detected: {}", e.getMessage(), e);
        ExceptionBody body = new ExceptionBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.ILLEGAL_STATE,
                "Внутренняя ошибка сервера (недопустимое состояние)."
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionBody> handleException(Exception e) {
        log.error("Unhandled exception occurred: {}", e.getMessage(), e);
        ExceptionBody body = new ExceptionBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.INTERNAL_ERROR,
                "Произошла внутренняя ошибка сервера. Пожалуйста, обратитесь к администратору."
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionBody> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message;
        String errorCode;
        HttpStatus status;

        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException) {
            ConstraintViolationException consEx = (ConstraintViolationException) cause;
            String constraintName = consEx.getConstraintName();
            if (constraintName != null) {
                if (constraintName.contains("technics_serial_number_key")) {
                    message = "Техника с указанным серийным номером уже существует.";
                    errorCode = ErrorCodes.DUPLICATE_ENTITY;
                    status = HttpStatus.CONFLICT; // 409
                } else if (constraintName.contains("technics_vin_key")) {
                    message = "Техника с указанным VIN уже существует.";
                    errorCode = ErrorCodes.DUPLICATE_ENTITY;
                    status = HttpStatus.CONFLICT; // 409
                } else {
                    message = "Нарушено ограничение базы данных: " + constraintName;
                    errorCode = ErrorCodes.BAD_REQUEST;
                    status = HttpStatus.BAD_REQUEST;
                }
            } else {
                message = "Нарушено ограничение базы данных.";
                errorCode = ErrorCodes.BAD_REQUEST;
                status = HttpStatus.BAD_REQUEST;
            }
        } else {

            message = ex.getMostSpecificCause().getMessage();
            errorCode = ErrorCodes.BAD_REQUEST;
            status = HttpStatus.BAD_REQUEST;
        }

        log.warn("Data integrity violation: {}", message, ex);
        ExceptionBody body = new ExceptionBody(status, errorCode, message);
        return new ResponseEntity<>(body, status);
    }
}