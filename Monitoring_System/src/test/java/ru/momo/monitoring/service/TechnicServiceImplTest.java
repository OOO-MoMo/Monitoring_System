package ru.momo.monitoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.exceptions.SensorBadRequestException;
import ru.momo.monitoring.services.CompanyService;
import ru.momo.monitoring.services.SecurityService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.services.impl.TechnicServiceImpl;
import ru.momo.monitoring.store.dto.request.TechnicCreateRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicPutDriverRequestDto;
import ru.momo.monitoring.store.dto.request.TechnicUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.TechnicCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicPutDriverResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicResponseDto;
import ru.momo.monitoring.store.dto.response.TechnicUnassignDriverResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.repositories.TechnicRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class TechnicServiceImplTest {

    @Mock
    private TechnicRepository technicRepository;
    @Mock
    private UserService userService;
    @Mock
    private CompanyService companyService;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TechnicServiceImpl technicService;

    private UUID technicId;
    private UUID companyId;
    private UUID driverId;
    private UUID managerId;
    private String managerEmail;
    private Technic testTechnic;
    private Company testCompany;
    private User testDriver;
    private User testManager;
    private TechnicCreateRequestDto createRequest;

    @BeforeEach
    void setUp() {
        technicId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        managerEmail = "manager@example.com";

        testCompany = Company.builder().id(companyId).name("Test Company").users(new ArrayList<>()).technics(new ArrayList<>()).build();
        testDriver = User.builder().id(driverId).email("driver@example.com").role(RoleName.ROLE_DRIVER).company(testCompany).technics(new ArrayList<>()).build();
        testManager = User.builder().id(managerId).email(managerEmail).role(RoleName.ROLE_MANAGER).company(testCompany).build();

        testTechnic = Technic.builder()
                .id(technicId)
                .brand("CAT")
                .model("320D")
                .year(2020)
                .serialNumber("SN123")
                .vin("VIN123")
                .company(testCompany)
                .isActive(true)
                .sensors(new ArrayList<>())
                .build();

        createRequest = TechnicCreateRequestDto.builder()
                .brand("John Deere")
                .model("8R")
                .year(2022)
                .serialNumber("SN_JD_001")
                .vin("VIN_JD_001")
                .description("Powerful tractor")
                .companyId(companyId)
                .build();
    }

    @Test
    void getTechById_ShouldReturnDto() {
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        TechnicResponseDto result = technicService.getTechById(technicId);
        assertNotNull(result);
        assertEquals(testTechnic.getId(), result.getTechnicId());
        assertEquals(testTechnic.getBrand(), result.getBrand());
    }

    @Test
    void getTechById_WhenNotFound_ShouldThrowException() {
        when(technicRepository.findByIdOrThrow(technicId)).thenThrow(new ResourceNotFoundException("Technic not found"));
        assertThrows(ResourceNotFoundException.class, () -> technicService.getTechById(technicId));
    }

    @Test
    void create_ShouldSaveAndReturnCreatedDto() {
        when(companyService.findById(createRequest.getCompanyId())).thenReturn(testCompany);
        doNothing().when(technicRepository).throwIfExistWithSameSerialNumber(createRequest.getSerialNumber());
        doNothing().when(technicRepository).throwIfExistWithSameVin(createRequest.getVin());

        when(technicRepository.save(any(Technic.class))).thenAnswer(invocation -> {
            Technic t = invocation.getArgument(0);
            if (t.getId() == null) t.setId(UUID.randomUUID()); // Имитируем генерацию ID
            return t;
        });
        doNothing().when(companyService).save(any(Company.class));


        TechnicCreatedResponseDto result = technicService.create(createRequest);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(createRequest.getBrand(), result.getBrand());
        assertTrue(testCompany.getTechnics().stream().anyMatch(t -> t.getSerialNumber().equals(createRequest.getSerialNumber()))); // Проверяем добавление в компанию

        verify(technicRepository).save(any(Technic.class));
        verify(companyService).save(testCompany);
    }

    @Test
    void create_WhenSerialNumberExists_ShouldThrowException() {
        when(companyService.findById(createRequest.getCompanyId())).thenReturn(testCompany);
        doThrow(new EntityDuplicationException("Serial number exists")).when(technicRepository).throwIfExistWithSameSerialNumber(createRequest.getSerialNumber());

        assertThrows(EntityDuplicationException.class, () -> technicService.create(createRequest));
        verify(technicRepository, never()).save(any(Technic.class));
    }

    @Test
    void create_WhenVinExists_ShouldThrowException() {
        when(companyService.findById(createRequest.getCompanyId())).thenReturn(testCompany);
        doNothing().when(technicRepository).throwIfExistWithSameSerialNumber(createRequest.getSerialNumber());
        doThrow(new EntityDuplicationException("VIN exists")).when(technicRepository).throwIfExistWithSameVin(createRequest.getVin());

        assertThrows(EntityDuplicationException.class, () -> technicService.create(createRequest));
        verify(technicRepository, never()).save(any(Technic.class));
    }

    @Test
    void getFilteredTechnics_ShouldReturnFilteredList() {
        when(userService.getByEmail(managerEmail)).thenReturn(testManager);
        when(technicRepository.findAll(any(Specification.class))).thenReturn(List.of(testTechnic));

        List<TechnicResponseDto> result = technicService.getFilteredTechnics(managerEmail, null, 2020, "CAT", null, true);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testTechnic.getId(), result.get(0).getTechnicId());
    }

    @Test
    void putNewDriver_ShouldAssignDriverAndReturnDto() {
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        when(userService.getByIdEntity(driverId)).thenReturn(testDriver);
        when(technicRepository.save(any(Technic.class))).thenReturn(testTechnic);

        TechnicPutDriverRequestDto request = new TechnicPutDriverRequestDto(technicId, driverId);
        TechnicPutDriverResponseDto result = technicService.putNewDriver(request);

        assertNotNull(result);
        assertEquals(technicId, result.technicId());
        assertEquals(driverId, result.driverId());
        assertEquals(testDriver, testTechnic.getOwnerId());
        assertTrue(testDriver.getTechnics().contains(testTechnic));
        verify(technicRepository).save(testTechnic);
    }

    @Test
    void findByCompanyAndId_WhenTechnicInCompany_ShouldReturnTechnic() {
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        Technic result = technicService.findByCompanyAndId(companyId, technicId);
        assertEquals(testTechnic, result);
    }

    @Test
    void findByCompanyAndId_WhenTechnicNotInCompany_ShouldThrowException() {
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        UUID otherCompanyId = UUID.randomUUID();
        assertThrows(SensorBadRequestException.class,
                () -> technicService.findByCompanyAndId(otherCompanyId, technicId));
    }

    @Test
    void save_ShouldCallRepositorySave() {
        technicService.save(testTechnic);
        verify(technicRepository).save(testTechnic);
    }

    @Test
    void getEntityById_ShouldReturnEntity() {
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        Technic result = technicService.getEntityById(technicId);
        assertEquals(testTechnic, result);
    }

    // --- Тесты для update ---
    @Test
    void update_WhenAdmin_ShouldUpdateTechnic() {
        User adminUser = User.builder().id(UUID.randomUUID()).role(RoleName.ROLE_ADMIN).company(testCompany).build();
        when(securityService.getCurrentUser()).thenReturn(adminUser);
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        when(technicRepository.save(any(Technic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TechnicUpdateRequestDto request = new TechnicUpdateRequestDto(
                "UpdatedBrand",
                "UpdatedModel",
                null,
                null,
                null,
                null,
                null
        );
        TechnicResponseDto result = technicService.update(technicId, request);

        assertEquals("UpdatedBrand", result.getBrand());
        assertEquals("UpdatedModel", result.getModel());
        verify(technicRepository).save(testTechnic);
    }

    @Test
    void update_WhenManagerUpdatesOwnCompanyTechnic_ShouldUpdate() {
        when(securityService.getCurrentUser()).thenReturn(testManager);
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        when(technicRepository.save(any(Technic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TechnicUpdateRequestDto request = new TechnicUpdateRequestDto(
                null,
                null,
                2023,
                null,
                null,
                null,
                null
        );
        TechnicResponseDto result = technicService.update(technicId, request);

        assertEquals(2023, result.getYear());
    }

    @Test
    void update_WhenManagerUpdatesOtherCompanyTechnic_ShouldThrowAccessDenied() {
        Company otherCompany = Company.builder().id(UUID.randomUUID()).name("Other Company").build();
        Technic otherCompanyTechnic = Technic.builder().id(UUID.randomUUID()).company(otherCompany).build();

        when(securityService.getCurrentUser()).thenReturn(testManager);
        when(technicRepository.findByIdOrThrow(otherCompanyTechnic.getId())).thenReturn(otherCompanyTechnic);

        TechnicUpdateRequestDto request = new TechnicUpdateRequestDto(
                "AttemptUpdate",
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(AccessDeniedException.class,
                () -> technicService.update(otherCompanyTechnic.getId(), request));
        verify(technicRepository, never()).save(any());
    }

    // --- Тесты для delete ---
    @Test
    void delete_WhenAdmin_ShouldDeleteTechnic() {
        User adminUser = User.builder().id(UUID.randomUUID()).role(RoleName.ROLE_ADMIN).company(testCompany).build();
        when(securityService.getCurrentUser()).thenReturn(adminUser);
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        doNothing().when(technicRepository).delete(testTechnic);

        assertDoesNotThrow(() -> technicService.delete(technicId));
        verify(technicRepository).delete(testTechnic);
    }

    @Test
    void delete_WhenManagerDeletesOtherCompanyTechnic_ShouldThrowAccessDenied() {
        Company otherCompany = Company.builder().id(UUID.randomUUID()).name("Other Company").build();
        Technic otherCompanyTechnic = Technic.builder().id(UUID.randomUUID()).company(otherCompany).build();

        when(securityService.getCurrentUser()).thenReturn(testManager);
        when(technicRepository.findByIdOrThrow(otherCompanyTechnic.getId())).thenReturn(otherCompanyTechnic);

        assertThrows(AccessDeniedException.class,
                () -> technicService.delete(otherCompanyTechnic.getId()));
        verify(technicRepository, never()).delete((Technic) any());
    }

    // --- Тесты для getAllTechnicsByCompanyId ---
    @Test
    void getAllTechnicsByCompanyId_ShouldReturnListOfDtos() {
        when(companyService.findById(companyId)).thenReturn(testCompany);
        when(technicRepository.findByCompanyId(companyId)).thenReturn(List.of(testTechnic));

        List<TechnicResponseDto> result = technicService.getAllTechnicsByCompanyId(companyId);
        assertFalse(result.isEmpty());
        assertEquals(testTechnic.getId(), result.get(0).getTechnicId());
    }

    @Test
    void getAllTechnicsByCompanyId_WhenCompanyNotFound_ShouldThrowExceptionFromCompanyService() {
        when(companyService.findById(companyId)).thenThrow(new ResourceNotFoundException("Company not found"));
        assertThrows(ResourceNotFoundException.class, () -> technicService.getAllTechnicsByCompanyId(companyId));
        verify(technicRepository, never()).findByCompanyId(any());
    }

    // --- Тесты для getTechnicsForDriver ---
    @Test
    void getTechnicsForDriver_WhenDriverHasTechnics_ShouldReturnList() {
        testDriver.setTechnics(List.of(testTechnic));
        when(userService.getByEmail(testDriver.getEmail())).thenReturn(testDriver);

        List<TechnicResponseDto> result = technicService.getTechnicsForDriver(testDriver.getEmail());
        assertFalse(result.isEmpty());
        assertEquals(testTechnic.getId(), result.get(0).getTechnicId());
    }

    @Test
    void getTechnicsForDriver_WhenDriverHasNoTechnics_ShouldReturnEmptyList() {
        testDriver.setTechnics(new ArrayList<>());
        when(userService.getByEmail(testDriver.getEmail())).thenReturn(testDriver);
        List<TechnicResponseDto> result = technicService.getTechnicsForDriver(testDriver.getEmail());
        assertTrue(result.isEmpty());
    }

    @Test
    void getTechnicsForDriver_WhenDriverTechnicsIsNull_ShouldReturnEmptyList() {
        testDriver.setTechnics(null);
        when(userService.getByEmail(testDriver.getEmail())).thenReturn(testDriver);
        List<TechnicResponseDto> result = technicService.getTechnicsForDriver(testDriver.getEmail());
        assertTrue(result.isEmpty());
    }

    // --- Тесты для getAllTechnicsForManager ---
    @Test
    void getAllTechnicsForManager_ShouldReturnManagerCompanyTechnics() {
        when(securityService.getCurrentUser()).thenReturn(testManager);
        when(technicRepository.findByCompanyId(companyId)).thenReturn(List.of(testTechnic));

        List<TechnicResponseDto> result = technicService.getAllTechnicsForManager();
        assertFalse(result.isEmpty());
        assertEquals(testTechnic.getId(), result.get(0).getTechnicId());
    }

    // --- Тесты для unassignDriverFromTechnic ---
    @Test
    void unassignDriverFromTechnic_WhenValid_ShouldUnassign() {
        testTechnic.setOwnerId(testDriver);
        testDriver.getTechnics().add(testTechnic);

        when(securityService.getCurrentUser()).thenReturn(testManager);
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        when(userService.getByIdEntity(driverId)).thenReturn(testDriver);
        when(technicRepository.save(any(Technic.class))).thenReturn(testTechnic);
        doNothing().when(userService).save(any(User.class));


        TechnicUnassignDriverResponseDto result = technicService.unassignDriverFromTechnic(technicId, driverId);

        assertNotNull(result);
        assertEquals(technicId, result.getTechnicId());
        assertEquals(driverId, result.getDriverId());
        assertNull(testTechnic.getOwnerId());
        assertFalse(testDriver.getTechnics().contains(testTechnic));
        verify(technicRepository).save(testTechnic);
        verify(userService).save(testDriver);
    }

    @Test
    void unassignDriverFromTechnic_WhenManagerFromDifferentCompany_ShouldThrowAccessDenied() {
        Company otherCompany = Company.builder().id(UUID.randomUUID()).build();
        User managerFromOtherCompany = User.builder().id(UUID.randomUUID()).role(RoleName.ROLE_MANAGER).company(otherCompany).build();
        testTechnic.setOwnerId(testDriver);

        when(securityService.getCurrentUser()).thenReturn(managerFromOtherCompany);
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        when(userService.getByIdEntity(driverId)).thenReturn(testDriver);

        assertThrows(AccessDeniedException.class,
                () -> technicService.unassignDriverFromTechnic(technicId, driverId));
    }

    @Test
    void unassignDriverFromTechnic_WhenDriverNotAssigned_ShouldThrowSensorBadRequest() {
        testTechnic.setOwnerId(null);

        when(securityService.getCurrentUser()).thenReturn(testManager);
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        when(userService.getByIdEntity(driverId)).thenReturn(testDriver);

        assertThrows(SensorBadRequestException.class,
                () -> technicService.unassignDriverFromTechnic(technicId, driverId));
    }

    @Test
    void unassignDriverFromTechnic_WhenDifferentDriverAssigned_ShouldThrowSensorBadRequest() {
        User anotherDriver = User.builder().id(UUID.randomUUID()).role(RoleName.ROLE_DRIVER).build();
        testTechnic.setOwnerId(anotherDriver);

        when(securityService.getCurrentUser()).thenReturn(testManager);
        when(technicRepository.findByIdOrThrow(technicId)).thenReturn(testTechnic);
        when(userService.getByIdEntity(driverId)).thenReturn(testDriver);

        assertThrows(SensorBadRequestException.class,
                () -> technicService.unassignDriverFromTechnic(technicId, driverId));
    }
}