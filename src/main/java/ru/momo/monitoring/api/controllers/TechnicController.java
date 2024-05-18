package ru.momo.monitoring.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.momo.monitoring.services.TechnicService;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicUpdateResponseDto;


import java.net.URI;


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
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getByUserId(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(technicService.getTechByUserId(id));
    }
    @PostMapping("/")
    public ResponseEntity<?> addNewTechnic(@RequestBody @Validated TechnicCreateRequestDto request) {
        TechnicCreatedResponseDto response = technicService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/technic/" + response.getTechnicId()))
                .body(response);
    }


    @PutMapping("/")
    public ResponseEntity<?> updateTechnic(@RequestBody @Validated TechnicUpdateRequestDto request) {
        TechnicUpdateResponseDto response = technicService.update(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTechnic(@PathVariable Long id) {
        technicService.delete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

}
