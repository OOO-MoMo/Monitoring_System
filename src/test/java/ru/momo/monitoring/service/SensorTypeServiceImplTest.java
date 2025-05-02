package ru.momo.monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.services.impl.SensorTypeServiceImpl;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.mapper.SensorTypeMapper;
import ru.momo.monitoring.store.repositories.SensorTypeRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorTypeServiceImplTest {

    @Mock
    private SensorTypeRepository sensorTypeRepository;

    @Mock
    private SensorTypeMapper sensorTypeMapper;

    @InjectMocks
    private SensorTypeServiceImpl sensorTypeService;

    private final UUID TEST_ID = UUID.randomUUID();
    private final String TEST_NAME = "Датчик температуры";

    @Test
    void createSensorType_ShouldSuccess() {
        // Arrange
        CreateSensorTypeRequest request = new CreateSensorTypeRequest(
                TEST_NAME,
                "°C",
                "Описание",
                Map.of("precision", 0.1)
        );

        SensorType entity = new SensorType();

        SensorTypeDto expectedDto = new SensorTypeDto(
                TEST_ID, TEST_NAME, "°C", "Описание", Map.of("precision", 0.1)
        );

        doNothing().when(sensorTypeRepository).throwIfExistsWithSameName(TEST_NAME);
        when(sensorTypeMapper.toEntity(request)).thenReturn(entity);
        when(sensorTypeRepository.save(entity)).thenReturn(entity);
        when(sensorTypeMapper.toDto(entity)).thenReturn(expectedDto);

        // Act
        SensorTypeDto result = sensorTypeService.createSensorType(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(sensorTypeRepository).throwIfExistsWithSameName(TEST_NAME);
        verify(sensorTypeRepository).save(entity);
    }

    @Test
    void createSensorType_ShouldThrowWhenNameExists() {
        // Arrange
        CreateSensorTypeRequest request = new CreateSensorTypeRequest(
                TEST_NAME, "°C", null, null
        );

        doThrow(new EntityDuplicationException("Error"))
                .when(sensorTypeRepository)
                .throwIfExistsWithSameName(TEST_NAME);

        // Act & Assert
        assertThrows(EntityDuplicationException.class,
                () -> sensorTypeService.createSensorType(request));
        verify(sensorTypeRepository, never()).save(any());
    }

    @Test
    void getAllSensorTypes_ShouldReturnList() {
        // Arrange
        SensorType entity = new SensorType();
        SensorTypeDto dto = new SensorTypeDto(TEST_ID, TEST_NAME, "°C", null, null);

        when(sensorTypeRepository.findAll()).thenReturn(List.of(entity));
        when(sensorTypeMapper.toDto(entity)).thenReturn(dto);

        // Act
        SensorTypesDto result = sensorTypeService.getAllSensorTypes();

        // Assert
        assertEquals(1, result.sensorTypesDTOList().size());
        assertEquals(dto, result.sensorTypesDTOList().get(0));
        verify(sensorTypeRepository).findAll();
    }

    @Test
    void getSensorTypeById_ShouldReturnDto() {
        // Arrange
        SensorType entity = new SensorType();
        SensorTypeDto expectedDto = new SensorTypeDto(TEST_ID, TEST_NAME, "°C", null, null);

        when(sensorTypeRepository.getByIdOrThrow(TEST_ID)).thenReturn(entity);
        when(sensorTypeMapper.toDto(entity)).thenReturn(expectedDto);

        // Act
        SensorTypeDto result = sensorTypeService.getSensorTypeById(TEST_ID);

        // Assert
        assertEquals(expectedDto, result);
    }

    @Test
    void getSensorTypeById_ShouldThrowWhenNotFound() {
        // Arrange
        when(sensorTypeRepository.getByIdOrThrow(TEST_ID)).thenThrow(ResourceNotFoundException.class);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> sensorTypeService.getSensorTypeById(TEST_ID));
    }
}