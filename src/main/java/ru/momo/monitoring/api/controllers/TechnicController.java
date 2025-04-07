package ru.momo.monitoring.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import ru.momo.monitoring.store.dto.response.TechnicUpdateResponseDto;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/technic")
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

    @GetMapping("/")
    public ResponseEntity<?> getByUserId(
            @RequestParam(name = "userId") UUID userId,
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "offset", required = false, defaultValue = "20") Integer offset,
            @RequestParam(name = "brand", required = false, defaultValue = "") String brand,
            @RequestParam(name = "model", required = false, defaultValue = "") String model) {
        Page<TechnicResponseDto> response = technicService.getTechByUserId(
                userId,
                PageRequest.of(page, offset),
                brand,
                model
        );

        if (response.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updateTechnic(@RequestBody @Validated TechnicUpdateRequestDto request) {
        TechnicUpdateResponseDto response = technicService.update(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTechnic(@PathVariable UUID id) {
        technicService.delete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/data/{id}")
    public ResponseEntity<?> getSensorsData(@PathVariable UUID id) {
        TechnicDataResponseDto response = technicService.getSensorsData(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
