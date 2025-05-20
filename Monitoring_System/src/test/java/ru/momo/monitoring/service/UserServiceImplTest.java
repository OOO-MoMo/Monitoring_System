package ru.momo.monitoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.exceptions.UserBadRequestException;
import ru.momo.monitoring.services.impl.UserServiceImpl;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.CompanyIdResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserRoleResponseDto;
import ru.momo.monitoring.store.dto.response.UsersResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.repositories.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserData testUserData;
    private Company testCompany;
    private final UUID userId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final String email = "test@example.com";
    private final String phoneNumber = "+79001234567";

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(companyId);
        testCompany.setName("Test Corp");

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(email);
        testUser.setCompany(testCompany);
        testUser.setRole(RoleName.ROLE_MANAGER);
        testUser.setIsActive(true);
        testUser.setIsConfirmed(true);

        testUserData = new UserData();
        testUserData.setUser(testUser);
        testUserData.setFirstname("Тест");
        testUserData.setLastname("Тестов");
        testUserData.setPhoneNumber(phoneNumber);
        testUser.setUserData(testUserData);
    }

    @Test
    void getById_ShouldReturnMappedUserResponseDto() {
        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);

        UserResponseDto result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByIdOrThrow(userId);
    }

    @Test
    void getByIdEntity_ShouldReturnUser() {
        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        User result = userService.getByIdEntity(userId);
        assertEquals(testUser, result);
    }

    @Test
    void getByEmail_ShouldReturnUser() {
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        User result = userService.getByEmail(email);
        assertEquals(testUser, result);
    }


    // --- Тесты для метода update ---
    @Test
    void update_WhenUserDataIsNull_ShouldCreateAndSetUserData() {
        User userWithoutData = new User();
        userWithoutData.setEmail(email);
        userWithoutData.setCompany(testCompany);

        UserUpdateRequestDto request = UserUpdateRequestDto.builder().firstname("НовоеИмя").build();
        when(userRepository.findByEmailOrThrow(email)).thenReturn(userWithoutData);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        userService.update(request, email);

        assertNotNull(userWithoutData.getUserData());
        assertEquals("НовоеИмя", userWithoutData.getUserData().getFirstname());
        verify(userRepository).save(userWithoutData);
    }

    @Test
    void update_WhenPhoneNumberExists_ShouldThrowException() {
        UserUpdateRequestDto request = UserUpdateRequestDto.builder().phoneNumber(phoneNumber).build();
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        when(userRepository.existsByUserData_PhoneNumber(phoneNumber)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.update(request, email));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_WithValidData_ShouldUpdateAndSaveUser() {
        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .firstname("Иван")
                .lastname("Иванов")
                .patronymic("Иванович")
                .phoneNumber("+79998887766")
                .address("Новый адрес")
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .build();

        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        when(userRepository.existsByUserData_PhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);


        UserResponseDto result = userService.update(request, email);

        assertNotNull(result);
        assertEquals("Иван", testUser.getUserData().getFirstname());
        assertEquals("+79998887766", testUser.getUserData().getPhoneNumber());
        verify(userRepository).save(testUser);
    }


    @Test
    void delete_ShouldDeactivateUser() {
        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        userService.delete(userId);
        assertFalse(testUser.getIsActive());
        verify(userRepository).save(testUser);
    }

    @Test
    void confirmUser_ShouldSetConfirmedTrue() {
        testUser.setIsConfirmed(false);
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.confirmUser(email);
        assertTrue(result.getIsConfirmed());
    }


    // --- Тесты для getNewUserRoleByCurrentUser ---
    @Test
    void getNewUserRoleByCurrentUser_WhenAdmin_ShouldReturnManager() {
        testUser.setRole(RoleName.ROLE_ADMIN);
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        assertEquals(RoleName.ROLE_MANAGER, userService.getNewUserRoleByCurrentUser(email));
    }

    @Test
    void getNewUserRoleByCurrentUser_WhenManager_ShouldReturnDriver() {
        testUser.setRole(RoleName.ROLE_MANAGER);
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        assertEquals(RoleName.ROLE_DRIVER, userService.getNewUserRoleByCurrentUser(email));
    }

    @Test
    void getNewUserRoleByCurrentUser_WhenDriver_ShouldThrowException() {
        testUser.setRole(RoleName.ROLE_DRIVER);
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        assertThrows(IllegalStateException.class, () -> userService.getNewUserRoleByCurrentUser(email));
    }

    @Test
    void getCurrentUserByEmail_ShouldReturnUserResponseDto() {
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        UserResponseDto result = userService.getCurrentUserByEmail(email);
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getCurrentUserRoleByEmail_ShouldReturnRoleName() {
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        UserRoleResponseDto result = userService.getCurrentUserRoleByEmail(email);
        assertEquals(testUser.getRole().name(), result.roleName());
    }

    @Test
    void save_ShouldCallRepositorySave() {
        userService.save(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void findAllActiveByCompanyId_ShouldReturnUsersList() {
        List<User> users = List.of(testUser);
        when(userRepository.findUserByCompany_Id(companyId)).thenReturn(users);
        List<User> result = userService.findAllActiveByCompanyId(companyId);
        assertEquals(users, result);
    }

    // --- Тесты для методов поиска (search*) ---
    @Test
    void searchActiveDrivers_WhenManagerHasCompany_ShouldFilterByCompany() {
        User manager = new User();
        manager.setEmail("manager@example.com");
        manager.setCompany(testCompany);

        when(userRepository.findByEmailOrThrow("manager@example.com")).thenReturn(manager);
        when(userRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testUser));

        UsersResponseDto result = userService.searchActiveDrivers(null, null, null, "manager@example.com");
        assertFalse(result.users().isEmpty());
    }

    @Test
    void searchManagers_ShouldReturnFilteredManagers() {
        when(userRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testUser));
        UsersResponseDto result = userService.searchManagers(companyId, "Тест", null, null, true);
        assertFalse(result.users().isEmpty());
    }

    @Test
    void searchDrivers_ShouldReturnFilteredDrivers() {
        when(userRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testUser));
        UsersResponseDto result = userService.searchDrivers(companyId, "Тест", null, null, false); // isActive = false
        assertFalse(result.users().isEmpty());
    }


    @Test
    void getCompanyIdForManager_ShouldReturnCorrectCompanyId() {
        when(userRepository.findByEmailOrThrow(email)).thenReturn(testUser);
        CompanyIdResponseDto result = userService.getCompanyIdForManager(email);
        assertEquals(testCompany.getId(), result.uuid());
    }

    // --- Тесты для updateById ---
    @Test
    void updateById_WhenActorIsAdmin_ShouldUpdateUser() {
        User adminActor = new User();
        adminActor.setRole(RoleName.ROLE_ADMIN);
        UserUpdateRequestDto request = UserUpdateRequestDto.builder().firstname("Updated").build();

        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        when(userRepository.findByEmailOrThrow("admin_actor@example.com")).thenReturn(adminActor);
        when(userRepository.save(any(User.class))).thenReturn(testUser);


        UserResponseDto result = userService.updateById(userId, request, "admin_actor@example.com");
        assertEquals("Updated", result.getFirstname());
    }

    @Test
    void updateById_WhenManagerUpdatesUserInSameCompany_ShouldUpdate() {
        User managerActor = new User();
        managerActor.setRole(RoleName.ROLE_MANAGER);
        managerActor.setCompany(testCompany);
        UserUpdateRequestDto request = UserUpdateRequestDto.builder().firstname("UpdatedByManager").build();

        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        when(userRepository.findByEmailOrThrow("manager_actor@example.com")).thenReturn(managerActor);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.updateById(userId, request, "manager_actor@example.com");
        assertEquals("UpdatedByManager", result.getFirstname());
    }

    @Test
    void updateById_WhenManagerUpdatesUserInDifferentCompany_ShouldThrowAccessDenied() {
        User managerActor = new User();
        managerActor.setRole(RoleName.ROLE_MANAGER);
        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        managerActor.setCompany(otherCompany);
        UserUpdateRequestDto request = UserUpdateRequestDto.builder().build();

        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        when(userRepository.findByEmailOrThrow("manager_actor@example.com")).thenReturn(managerActor);

        assertThrows(AccessDeniedException.class, () -> userService.updateById(userId, request, "manager_actor@example.com"));
    }

    @Test
    void updateById_WhenActorIsNotAdminOrManager_ShouldThrowAccessDenied() {
        User driverActor = new User();
        driverActor.setRole(RoleName.ROLE_DRIVER);
        UserUpdateRequestDto request = UserUpdateRequestDto.builder().build();

        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        when(userRepository.findByEmailOrThrow("driver_actor@example.com")).thenReturn(driverActor);

        assertThrows(AccessDeniedException.class, () -> userService.updateById(userId, request, "driver_actor@example.com"));
    }

    @Test
    void updateById_WhenNewPhoneNumberIsTaken_ShouldThrowUserBadRequestException() {
        String takenPhoneNumber = "+70000000000";
        User adminActor = new User();
        adminActor.setRole(RoleName.ROLE_ADMIN);
        UserUpdateRequestDto request = UserUpdateRequestDto.builder().phoneNumber(takenPhoneNumber).build();

        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        when(userRepository.findByEmailOrThrow("admin@example.com")).thenReturn(adminActor);
        when(userRepository.existsByUserData_PhoneNumberAndIdNot(takenPhoneNumber, userId)).thenReturn(true);

        assertThrows(UserBadRequestException.class, () -> userService.updateById(userId, request, "admin@example.com"));
    }

    @Test
    void updateById_WhenPhoneNumberIsBlank_ShouldSetPhoneNumberToNull() {
        User adminActor = new User();
        adminActor.setRole(RoleName.ROLE_ADMIN);
        UserUpdateRequestDto request = UserUpdateRequestDto.builder().phoneNumber("   ").build();
        testUser.getUserData().setPhoneNumber("oldNumber");


        when(userRepository.findByIdOrThrow(userId)).thenReturn(testUser);
        when(userRepository.findByEmailOrThrow("admin@example.com")).thenReturn(adminActor);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        userService.updateById(userId, request, "admin@example.com");
        assertNull(testUser.getUserData().getPhoneNumber());
    }


    // --- Тесты для deactivateDriverByManager ---
    @Test
    void deactivateDriverByManager_WhenManagerAndDriverInSameCompany_ShouldDeactivate() {
        User manager = testUser;
        User driver = new User();
        driver.setId(UUID.randomUUID());
        driver.setRole(RoleName.ROLE_DRIVER);
        driver.setCompany(testCompany);
        driver.setIsActive(true);

        when(userRepository.findByEmailOrThrow(manager.getEmail())).thenReturn(manager);
        when(userRepository.findByIdOrThrow(driver.getId())).thenReturn(driver);

        userService.deactivateDriverByManager(driver.getId(), manager.getEmail());

        assertFalse(driver.getIsActive());
        verify(userRepository).save(driver);
    }

    @Test
    void deactivateDriverByManager_WhenTargetIsNotDriver_ShouldThrowUserBadRequest() {
        User manager = testUser;
        User notADriver = new User();
        notADriver.setId(UUID.randomUUID());
        notADriver.setRole(RoleName.ROLE_MANAGER);
        notADriver.setCompany(testCompany);

        when(userRepository.findByEmailOrThrow(manager.getEmail())).thenReturn(manager);
        when(userRepository.findByIdOrThrow(notADriver.getId())).thenReturn(notADriver);

        assertThrows(UserBadRequestException.class, () -> userService.deactivateDriverByManager(notADriver.getId(), manager.getEmail()));
    }

    @Test
    void deactivateDriverByManager_WhenManagerAndDriverInDifferentCompanies_ShouldThrowAccessDenied() {
        User manager = testUser;
        User driver = new User();
        driver.setId(UUID.randomUUID());
        driver.setRole(RoleName.ROLE_DRIVER);
        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        driver.setCompany(otherCompany); // Другая компания
        driver.setIsActive(true);

        when(userRepository.findByEmailOrThrow(manager.getEmail())).thenReturn(manager);
        when(userRepository.findByIdOrThrow(driver.getId())).thenReturn(driver);

        assertThrows(AccessDeniedException.class, () -> userService.deactivateDriverByManager(driver.getId(), manager.getEmail()));
    }

    // --- Тесты для saveNotConfirmedUser ---
    @Test
    void saveNotConfirmedUser_WhenPasswordsDoNotMatch_ShouldThrowUserBadRequest() {
        RegisterRequest request = new RegisterRequest("user@example.com", "pass1", "pass2", companyId, "First", "Last", "123");
        assertThrows(UserBadRequestException.class, () -> userService.saveNotConfirmedUser(request, RoleName.ROLE_DRIVER, testCompany));
    }

    @Test
    void saveNotConfirmedUser_WithValidData_ShouldSaveUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password", "password", companyId, "First", "Last", "123");
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.saveNotConfirmedUser(request, RoleName.ROLE_DRIVER, testCompany);

        assertEquals("encodedPassword", result.getPassword());
        assertEquals("user@example.com", result.getEmail());
        assertEquals(RoleName.ROLE_DRIVER, result.getRole());
        assertEquals(testCompany, result.getCompany());
        assertNotNull(result.getUserData());
        assertEquals("First", result.getUserData().getFirstname());
        assertFalse(result.getIsConfirmed());
        assertTrue(result.getIsActive());
        assertNotNull(result.getTechnics());
        assertTrue(testCompany.getUsers().contains(result));
    }
}