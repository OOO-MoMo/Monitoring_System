package ru.momo.monitoring.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.UserUpdateResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getById(id));
    }

    @PutMapping("/")
    public ResponseEntity<?> updateUser(@RequestBody @Validated UserUpdateRequestDto request) {
        UserUpdateResponseDto response = userService.update(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

}
