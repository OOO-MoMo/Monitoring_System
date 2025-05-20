package ru.momo.monitoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.services.SensorService;
import ru.momo.monitoring.services.impl.SensorTypeServiceImpl;
import ru.momo.monitoring.store.dto.request.CreateSensorTypeRequest;
import ru.momo.monitoring.store.dto.request.SensorTypeDto;
import ru.momo.monitoring.store.dto.request.UpdateSensorTypeRequest;
import ru.momo.monitoring.store.dto.response.SensorTypesDto;
import ru.momo.monitoring.store.entities.SensorType;
import ru.momo.monitoring.store.repositories.SensorTypeRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private SensorService sensorService;

    @InjectMocks
    private SensorTypeServiceImpl sensorTypeService;

    private final UUID TEST_ID = UUID.randomUUID();
    private final String TEST_NAME = "Датчик температуры";
    private final String TEST_UNIT = "°C";
    private final String TEST_DESCRIPTION = "Описание датчика температуры";
    private final Map<String, Object> TEST_METADATA = Map.of("precision", 0.1, "interface", "I2C");

    private CreateSensorTypeRequest createRequest;
    private SensorType sensorTypeEntity;
    private SensorTypeDto sensorTypeDto;

    @BeforeEach
    void setUp() {
        sensorTypeService.setSensorService(sensorService);

        createRequest = new CreateSensorTypeRequest(
                TEST_NAME,
                TEST_UNIT,
                TEST_DESCRIPTION,
                new HashMap<>(TEST_METADATA)
        );

        sensorTypeEntity = new SensorType();
        sensorTypeEntity.setId(TEST_ID);
        sensorTypeEntity.setName(TEST_NAME);
        sensorTypeEntity.setUnit(TEST_UNIT);
        sensorTypeEntity.setDescription(TEST_DESCRIPTION);
        sensorTypeEntity.setMetadata(new HashMap<>(TEST_METADATA));

        sensorTypeDto = SensorTypeDto.fromEntity(sensorTypeEntity);
    }

    @Test
    void createSensorType_ShouldSuccess() {
        // Arrange
        SensorType entityToSave = new SensorType();
        entityToSave.setName(createRequest.name());
        entityToSave.setUnit(createRequest.unit());
        entityToSave.setDescription(createRequest.description());
        entityToSave.setMetadata(new HashMap<>(createRequest.metadata()));

        SensorType savedEntity = new SensorType();
        savedEntity.setId(TEST_ID);
        savedEntity.setName(createRequest.name());
        savedEntity.setUnit(createRequest.unit());
        savedEntity.setDescription(createRequest.description());
        savedEntity.setMetadata(new HashMap<>(createRequest.metadata()));


        doNothing().when(sensorTypeRepository).throwIfExistsWithSameName(TEST_NAME);
        when(sensorTypeRepository.save(any(SensorType.class))).thenReturn(savedEntity);

        // Act
        SensorTypeDto result = sensorTypeService.createSensorType(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        assertEquals(TEST_UNIT, result.getUnit());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        assertEquals(TEST_METADATA, result.getMetadata());

        verify(sensorTypeRepository).throwIfExistsWithSameName(TEST_NAME);
        verify(sensorTypeRepository).save(any(SensorType.class));
    }

    @Test
    void createSensorType_ShouldThrowWhenNameExists() {
        // Arrange
        doThrow(new EntityDuplicationException("SensorType with this name already exists"))
                .when(sensorTypeRepository)
                .throwIfExistsWithSameName(TEST_NAME);

        // Act & Assert
        EntityDuplicationException exception = assertThrows(EntityDuplicationException.class,
                () -> sensorTypeService.createSensorType(createRequest));
        assertEquals("SensorType with this name already exists", exception.getMessage());
        verify(sensorTypeRepository, never()).save(any());
    }

    @Test
    void createSensorType_WhenMetadataIsNullInRequest_ShouldSetEmptyMapInEntity() {
        CreateSensorTypeRequest requestWithNullMetadata = new CreateSensorTypeRequest(
                "New Sensor", "units", "desc", null
        );
        SensorType savedEntity = new SensorType();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setName(requestWithNullMetadata.name());
        savedEntity.setUnit(requestWithNullMetadata.unit());
        savedEntity.setDescription(requestWithNullMetadata.description());
        savedEntity.setMetadata(new HashMap<>());

        doNothing().when(sensorTypeRepository).throwIfExistsWithSameName(requestWithNullMetadata.name());
        when(sensorTypeRepository.save(any(SensorType.class))).thenAnswer(invocation -> {
            SensorType arg = invocation.getArgument(0);
            SensorType returned = new SensorType();
            returned.setId(UUID.randomUUID());
            returned.setName(arg.getName());
            returned.setUnit(arg.getUnit());
            returned.setDescription(arg.getDescription());
            returned.setMetadata(new HashMap<>(arg.getMetadata()));
            return returned;
        });

        SensorTypeDto result = sensorTypeService.createSensorType(requestWithNullMetadata);

        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().isEmpty());

        ArgumentCaptor<SensorType> captor = ArgumentCaptor.forClass(SensorType.class);
        verify(sensorTypeRepository).save(captor.capture());
        assertNotNull(captor.getValue().getMetadata());
        assertTrue(captor.getValue().getMetadata().isEmpty());
    }


    @Test
    void getAllSensorTypes_ShouldReturnList() {
        // Arrange
        when(sensorTypeRepository.findAll()).thenReturn(List.of(sensorTypeEntity));

        // Act
        SensorTypesDto result = sensorTypeService.getAllSensorTypes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.sensorTypesDTOList().size());
        SensorTypeDto dtoInList = result.sensorTypesDTOList().get(0);
        assertEquals(sensorTypeDto.getId(), dtoInList.getId());
        assertEquals(sensorTypeDto.getName(), dtoInList.getName());
        verify(sensorTypeRepository).findAll();
    }

    @Test
    void getAllSensorTypes_WhenNoTypes_ShouldReturnEmptyList() {
        when(sensorTypeRepository.findAll()).thenReturn(Collections.emptyList());
        SensorTypesDto result = sensorTypeService.getAllSensorTypes();
        assertNotNull(result);
        assertTrue(result.sensorTypesDTOList().isEmpty());
    }


    @Test
    void getSensorTypeById_ShouldReturnDto() {
        // Arrange
        when(sensorTypeRepository.getByIdOrThrow(TEST_ID)).thenReturn(sensorTypeEntity);

        // Act
        SensorTypeDto result = sensorTypeService.getSensorTypeById(TEST_ID);

        // Assert
        assertNotNull(result);
        assertEquals(sensorTypeDto.getId(), result.getId());
        assertEquals(sensorTypeDto.getName(), result.getName());
    }

    @Test
    void getSensorTypeById_ShouldThrowWhenNotFound() {
        // Arrange
        when(sensorTypeRepository.getByIdOrThrow(TEST_ID))
                .thenThrow(new ResourceNotFoundException("SensorType not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> sensorTypeService.getSensorTypeById(TEST_ID));
    }

    // --- Тесты для getSensorTypeEntityById ---
    @Test
    void getSensorTypeEntityById_ShouldReturnEntity() {
        when(sensorTypeRepository.getByIdOrThrow(TEST_ID)).thenReturn(sensorTypeEntity);
        SensorType result = sensorTypeService.getSensorTypeEntityById(TEST_ID);
        assertEquals(sensorTypeEntity, result);
    }

    @Test
    void getSensorTypeEntityById_ShouldThrowWhenNotFound() {
        when(sensorTypeRepository.getByIdOrThrow(TEST_ID))
                .thenThrow(new ResourceNotFoundException("SensorType not found"));
        assertThrows(ResourceNotFoundException.class,
                () -> sensorTypeService.getSensorTypeEntityById(TEST_ID));
    }

    // --- Тесты для updateSensorType ---
    @Test
    void updateSensorType_ShouldUpdateAndReturnDto() {
        UpdateSensorTypeRequest updateRequest = new UpdateSensorTypeRequest(
                "Новое имя", "m/s", "Новое описание", Map.of("new_key", "new_value")
        );
        SensorType existingEntity = new SensorType();
        existingEntity.setId(TEST_ID);
        existingEntity.setName(TEST_NAME);
        existingEntity.setUnit(TEST_UNIT);
        existingEntity.setDescription(TEST_DESCRIPTION);
        existingEntity.setMetadata(new HashMap<>(TEST_METADATA));

        when(sensorTypeRepository.getByIdOrThrow(TEST_ID)).thenReturn(existingEntity);
        doNothing().when(sensorTypeRepository).throwIfExistsWithSameNameAndDifferentId(updateRequest.name(), TEST_ID);
        when(sensorTypeRepository.save(any(SensorType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SensorTypeDto result = sensorTypeService.updateSensorType(TEST_ID, updateRequest);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals("Новое имя", result.getName());
        assertEquals("m/s", result.getUnit());
        assertEquals("Новое описание", result.getDescription());
        assertEquals(Map.of("new_key", "new_value"), result.getMetadata());

        verify(sensorTypeRepository).save(existingEntity);
        assertEquals("Новое имя", existingEntity.getName());
    }

    @Test
    void updateSensorType_WithNullValuesInRequest_ShouldNotUpdateThoseFields() {
        UpdateSensorTypeRequest updateRequest = new UpdateSensorTypeRequest(
                null,
                "m/s",
                null,
                null
        );
        SensorType existingEntity = new SensorType();
        existingEntity.setId(TEST_ID);
        existingEntity.setName(TEST_NAME);
        existingEntity.setUnit(TEST_UNIT);
        existingEntity.setDescription(TEST_DESCRIPTION);
        existingEntity.setMetadata(new HashMap<>(TEST_METADATA));


        when(sensorTypeRepository.getByIdOrThrow(TEST_ID)).thenReturn(existingEntity);
        when(sensorTypeRepository.save(any(SensorType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SensorTypeDto result = sensorTypeService.updateSensorType(TEST_ID, updateRequest);

        assertNotNull(result);
        assertEquals(TEST_NAME, result.getName());
        assertEquals("m/s", result.getUnit());
        assertNull(result.getDescription());
        assertEquals(TEST_METADATA, result.getMetadata());

        // Проверяем сущность
        assertEquals(TEST_NAME, existingEntity.getName());
        assertEquals("m/s", existingEntity.getUnit());
        assertNull(existingEntity.getDescription());
        assertEquals(TEST_METADATA, existingEntity.getMetadata());
    }


    @Test
    void updateSensorType_ShouldThrowWhenNameExistsForDifferentId() {
        UpdateSensorTypeRequest updateRequest = new UpdateSensorTypeRequest("Existing Name", "unit", null, null);
        doThrow(new EntityDuplicationException("Name already exists"))
                .when(sensorTypeRepository)
                .throwIfExistsWithSameNameAndDifferentId(updateRequest.name(), TEST_ID);

        assertThrows(EntityDuplicationException.class,
                () -> sensorTypeService.updateSensorType(TEST_ID, updateRequest));
        verify(sensorTypeRepository, never()).save(any());
    }

    // --- Тесты для deleteSensorType ---
    @Test
    void deleteSensorType_WhenNotUsed_ShouldDelete() {
        when(sensorTypeRepository.existsById(TEST_ID)).thenReturn(true);
        when(sensorService.existsByTypeId(TEST_ID)).thenReturn(false);
        doNothing().when(sensorTypeRepository).deleteById(TEST_ID);

        assertDoesNotThrow(() -> sensorTypeService.deleteSensorType(TEST_ID));
        verify(sensorTypeRepository).deleteById(TEST_ID);
    }

    @Test
    void deleteSensorType_WhenTypeNotFound_ShouldThrowResourceNotFound() {
        when(sensorTypeRepository.existsById(TEST_ID)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> sensorTypeService.deleteSensorType(TEST_ID));
        assertEquals("SensorType with id " + TEST_ID + " not found.", exception.getMessage());
        verify(sensorService, never()).existsByTypeId(any());
        verify(sensorTypeRepository, never()).deleteById(any());
    }

    @Test
    void deleteSensorType_WhenUsedBySensor_ShouldThrowSensorBadRequest() {
        when(sensorTypeRepository.existsById(TEST_ID)).thenReturn(true);
        when(sensorService.existsByTypeId(TEST_ID)).thenReturn(true);

        SensorBadRequestException exception = assertThrows(SensorBadRequestException.class,
                () -> sensorTypeService.deleteSensorType(TEST_ID));
        assertEquals("Cannot delete SensorType with ID " + TEST_ID + " because it is currently used by one or more Sensors.",
                exception.getMessage());
        verify(sensorTypeRepository, never()).deleteById(any());
    }
}