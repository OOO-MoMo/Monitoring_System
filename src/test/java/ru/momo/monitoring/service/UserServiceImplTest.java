package ru.momo.monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    private final UUID userId = UUID.randomUUID();

    private final String email = "test@example.com";

    @Test
    void getById_ShouldReturnMappedUserResponseDto() {
        User user = new User();
        user.setEmail(email);
        user.setCompany(new Company());
        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);

        UserResponseDto result = userService.getById(userId);

        assertEquals(email, result.getEmail());
        verify(userRepository).findByIdOrThrow(userId);
    }

    @Test
    void update_ShouldUpdateUserData() {
        // given
        User user = new User();
        user.setEmail(email);
        UserData userData = new UserData();
        user.setUserData(userData);
        user.setCompany(new Company());

        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .firstname("Иван")
                .lastname("Иванов")
                .patronymic("Иванович")
                .phoneNumber("+79001234567")
                .address("Москва")
                .dateOfBirth(LocalDate.of(1990, 5, 20))
                .build();

        when(userRepository.findByEmailOrThrow(email)).thenReturn(user);
        when(userRepository.existsByUserData_PhoneNumber(request.getPhoneNumber())).thenReturn(false);

        // when
        UserResponseDto response = userService.update(request, email);

        // then
        assertEquals("Иван", user.getUserData().getFirstname());
        assertEquals("Иванов", user.getUserData().getLastname());
        assertEquals("Иванович", user.getUserData().getPatronymic());
        assertEquals("+79001234567", user.getUserData().getPhoneNumber());

        verify(userRepository).save(user);
    }

    @Test
    void delete_ShouldDeactivateUser() {
        User user = new User();
        user.setCompany(new Company());
        user.setIsActive(true);

        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);

        userService.delete(userId);

        assertFalse(user.getIsActive());
        verify(userRepository).save(user);
    }

    @Test
    void confirmUser_ShouldSetConfirmedTrue() {
        User user = new User();
        user.setIsConfirmed(false);
        user.setEmail(email);
        user.setCompany(new Company());

        when(userRepository.findByEmailOrThrow(email)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.confirmUser(email);

        assertTrue(result.getIsConfirmed());
        verify(userRepository).save(user);
    }

    @Test
    void saveNotConfirmedUser_ShouldEncodePasswordAndSaveUser() {
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password",
                "password",
                UUID.randomUUID()
        );

        Company company = new Company();
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.saveNotConfirmedUser(request, RoleName.ROLE_MANAGER, company);

        verify(userRepository).save(userCaptor.capture());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());
        assertEquals(RoleName.ROLE_MANAGER, savedUser.getRole());
        assertEquals("newuser@example.com", savedUser.getEmail());
    }

    @Test
    void getNewUserRoleByCurrentUser_ShouldReturnCorrectRole() {
        User admin = new User();
        admin.setRole(RoleName.ROLE_ADMIN);

        when(userRepository.findByEmailOrThrow(email)).thenReturn(admin);

        RoleName nextRole = userService.getNewUserRoleByCurrentUser(email);
        assertEquals(RoleName.ROLE_MANAGER, nextRole);
    }

    @Test
    void getCurrentUserByEmail_ShouldReturnUserResponseDto() {
        User user = new User();
        user.setEmail(email);
        user.setCompany(new Company());
        when(userRepository.findByEmailOrThrow(email)).thenReturn(user);

        UserResponseDto result = userService.getCurrentUserByEmail(email);

        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmailOrThrow(email);
    }

    @Test
    void getCurrentUserRoleByEmail_ShouldReturnRoleName() {
        User user = new User();
        user.setEmail(email);
        user.setCompany(new Company());
        user.setRole(RoleName.ROLE_MANAGER);

        when(userRepository.findByEmailOrThrow(email)).thenReturn(user);

        UserRoleResponseDto result = userService.getCurrentUserRoleByEmail(email);

        assertEquals("ROLE_MANAGER", result.roleName());
    }

    @Test
    void save_ShouldCallRepositorySave() {
        User user = new User();
        user.setEmail(email);

        userService.save(user);

        verify(userRepository).save(user);
    }

    @Test
    void findAllActiveByCompanyId_ShouldReturnUsersList() {
        UUID companyId = UUID.randomUUID();
        List<User> users = List.of(new User(), new User());

        when(userRepository.findUserByCompany_Id(companyId)).thenReturn(users);

        List<User> result = userService.findAllActiveByCompanyId(companyId);

        assertEquals(2, result.size());
        verify(userRepository).findUserByCompany_Id(companyId);
    }

    @Test
    void getByIdEntity_ShouldReturnUser() {
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByIdOrThrow(userId)).thenReturn(user);

        User result = userService.getByIdEntity(userId);

        assertEquals(email, result.getEmail());
    }

    @Test
    void searchActiveDrivers_ShouldReturnFilteredDrivers() {
        Company company = new Company();
        company.setName("company");

        User user1 = new User();
        user1.setCompany(company);
        User user2 = new User();
        user2.setCompany(company);
        List<User> users = List.of(user1, user2);

        User manager = new User();
        manager.setEmail("manager@example.com");
        manager.setCompany(company);

        when(userRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(users);
        when(userService.getByEmail(any())).thenReturn(manager);

        UsersResponseDto result = userService.searchActiveDrivers(
                "Иван", "Иванов", "Иванович", manager.getEmail()
        );

        assertEquals(2, result.activeDrivers().size());
        verify(userRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void getCompanyIdForManager_shouldReturnCompanyId() {
        String email = "manager@example.com";
        UUID id = UUID.randomUUID();

        Company company = new Company();
        company.setId(id);

        User manager = new User();
        manager.setEmail(email);
        manager.setCompany(company);

        when(userService.getByEmail(email)).thenReturn(manager);

        CompanyIdResponseDto result = userService.getCompanyIdForManager(email);

        assertEquals(id, result.uuid());
    }

}
