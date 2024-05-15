package ru.momo.monitoring.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.momo.monitoring.services.TechnicService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/technic")
public class TechnicController {

    private final TechnicService technicService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(technicService.getTechById(id));
    }


}
