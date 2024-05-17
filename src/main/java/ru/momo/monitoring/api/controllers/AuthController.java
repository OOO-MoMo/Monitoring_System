package ru.momo.monitoring.api.controllers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.momo.monitoring.services.AuthService;
import ru.momo.monitoring.store.dto.request.JwtRequest;
import ru.momo.monitoring.store.dto.request.RefreshJwtRequest;
import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthController {

    final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Validated UserCreateRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated JwtRequest loginRequest){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshJwtRequest refreshToken){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.refresh(refreshToken));
    }

}
