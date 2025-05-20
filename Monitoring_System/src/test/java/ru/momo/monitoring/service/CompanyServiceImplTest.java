package ru.momo.monitoring.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.services.impl.CompanyServiceImpl;
import ru.momo.monitoring.store.dto.request.CompanyCreateRequestDto;
import ru.momo.monitoring.store.dto.request.CompanyUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.CompanyResponseDto;
import ru.momo.monitoring.store.dto.response.PageCompanyResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.repositories.CompanyRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company testCompany;
    private UUID testCompanyId;

    @BeforeEach
    void setUp() {
        testCompanyId = UUID.randomUUID();
        testCompany = Company.builder()
                .id(testCompanyId)
                .name("Тестовая Компания")
                .inn("1234567890")
                .headOfficeAddress("г. Тест, ул. Тестовая, д.1")
                .build();
    }

    @Test
    void create_ShouldSaveCompanyAndReturnDto() {
        // given
        CompanyCreateRequestDto request = new CompanyCreateRequestDto("ООО Ромашка", "7707083893", "г. Москва, ул. Ленина, д. 1");

        Company companyToSave = Company.builder()
                .name(request.name())
                .inn(request.inn())
                .headOfficeAddress(request.headOfficeAddress())
                .build();

        Company savedCompany = Company.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .inn(request.inn())
                .headOfficeAddress(request.headOfficeAddress())
                .build();

        doNothing().when(companyRepository).throwIfExistWithSameInn(request.inn());
        doNothing().when(companyRepository).throwIfExistWithSameName(request.name());
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // when
        CompanyResponseDto result = companyService.create(request);

        // then
        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.inn(), result.inn());
        assertEquals(request.headOfficeAddress(), result.headOfficeAddress());

        verify(companyRepository).save(argThat(companyArg ->
                companyArg.getName().equals(request.name()) &&
                        companyArg.getInn().equals(request.inn()) &&
                        companyArg.getHeadOfficeAddress().equals(request.headOfficeAddress())
        ));
    }

    @Test
    void delete_ShouldDeactivateUsersAndDeleteCompany() {
        // given
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setIsActive(true);
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setIsActive(true);
        List<User> users = List.of(user1, user2);

        when(companyRepository.findByIdOrThrow(testCompanyId)).thenReturn(testCompany);
        when(userService.findAllActiveByCompanyId(testCompanyId)).thenReturn(users);
        doAnswer(invocation -> invocation.getArgument(0)).when(userService).save(any(User.class));
        doNothing().when(companyRepository).delete(testCompany);


        // when
        companyService.delete(testCompanyId);

        // then
        verify(companyRepository).findByIdOrThrow(testCompanyId);
        verify(userService).findAllActiveByCompanyId(testCompanyId);
        assertFalse(user1.getIsActive());
        assertFalse(user2.getIsActive());
        verify(userService, times(users.size())).save(any(User.class));
        verify(companyRepository).delete(testCompany);
    }

    @Test
    void delete_WhenCompanyHasNoActiveUsers_ShouldOnlyDeleteCompany() {
        when(companyRepository.findByIdOrThrow(testCompanyId)).thenReturn(testCompany);
        when(userService.findAllActiveByCompanyId(testCompanyId)).thenReturn(Collections.emptyList());
        doNothing().when(companyRepository).delete(testCompany);

        companyService.delete(testCompanyId);

        verify(userService, never()).save(any(User.class));
        verify(companyRepository).delete(testCompany);
    }


    @Test
    void update_ShouldUpdateCompanyFieldsAndReturnDto() {
        // given
        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto(
                testCompanyId,
                "Обновленное имя",
                "0987654321",
                "Новый адрес"
        );


        Company originalCompany = Company.builder()
                .id(testCompanyId)
                .name("Старое имя")
                .inn("1234567890")
                .headOfficeAddress("Старый адрес")
                .build();

        when(companyRepository.findByIdOrThrow(testCompanyId)).thenReturn(originalCompany);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        if (request.inn() != null && !request.inn().equals(originalCompany.getInn())) {
            doNothing().when(companyRepository).throwIfExistWithSameInn(request.inn());
        }
        if (request.name() != null && !request.name().equals(originalCompany.getName())) {
            doNothing().when(companyRepository).throwIfExistWithSameName(request.name());
        }


        // when
        CompanyResponseDto result = companyService.update(request);

        // then
        assertNotNull(result);
        assertEquals(testCompanyId, result.uuid());
        assertEquals(request.name(), result.name());
        assertEquals(request.inn(), result.inn());
        assertEquals(request.headOfficeAddress(), result.headOfficeAddress());

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        Company savedCompany = companyCaptor.getValue();
        assertEquals(request.name(), savedCompany.getName());
        assertEquals(request.inn(), savedCompany.getInn());
        assertEquals(request.headOfficeAddress(), savedCompany.getHeadOfficeAddress());
    }

    @Test
    void findById_ShouldReturnCompany() {
        // given
        when(companyRepository.findByIdOrThrow(testCompanyId)).thenReturn(testCompany);

        // when
        Company result = companyService.findById(testCompanyId);

        // then
        assertNotNull(result);
        assertEquals(testCompany.getName(), result.getName());
        assertEquals(testCompany.getId(), result.getId());
        verify(companyRepository).findByIdOrThrow(testCompanyId);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        // given
        Company newCompany = Company.builder().name("Новая компания").build();

        // when
        companyService.save(newCompany);

        // then
        verify(companyRepository).save(newCompany);
    }

    @Test
    void findAll_ShouldReturnPagedCompanyResponseDtos_WhenNameIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 2);
        Company company1 = Company.builder().id(UUID.randomUUID()).name("Ромашка").inn("1").headOfficeAddress("МСК").build();
        Company company2 = Company.builder().id(UUID.randomUUID()).name("Одуванчик").inn("2").headOfficeAddress("СПБ").build();
        Page<Company> companyPage = new PageImpl<>(List.of(company1, company2), pageable, 2);

        when(companyRepository.findAllByNameContainingIgnoreCase("", pageable)).thenReturn(companyPage);

        // when
        PageCompanyResponseDto result = companyService.findAll(null, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("Ромашка", result.content().get(0).name());
        assertEquals(0, result.page());
        assertEquals(2, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
        verify(companyRepository).findAllByNameContainingIgnoreCase("", pageable);
    }

    @Test
    void findAll_ShouldReturnPagedCompanyResponseDtos_WhenNameIsProvided() {
        String searchName = "Рома";
        Pageable pageable = PageRequest.of(0, 1);
        Company company1 = Company.builder().id(UUID.randomUUID()).name("Ромашка").inn("1").headOfficeAddress("МСК").build();
        Page<Company> companyPage = new PageImpl<>(List.of(company1), pageable, 1);

        when(companyRepository.findAllByNameContainingIgnoreCase(searchName, pageable)).thenReturn(companyPage);

        PageCompanyResponseDto result = companyService.findAll(searchName, pageable);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("Ромашка", result.content().get(0).name());
        verify(companyRepository).findAllByNameContainingIgnoreCase(searchName, pageable);
    }


    // --- Тесты для getCompanyById ---
    @Test
    void getCompanyById_WhenCompanyExists_ShouldReturnCompanyResponseDto() {
        // given
        when(companyRepository.findByIdOrThrow(testCompanyId)).thenReturn(testCompany);

        // when
        CompanyResponseDto result = companyService.getCompanyById(testCompanyId);

        // then
        assertNotNull(result);
        assertEquals(testCompany.getId(), result.uuid());
        assertEquals(testCompany.getName(), result.name());
        assertEquals(testCompany.getInn(), result.inn());
        assertEquals(testCompany.getHeadOfficeAddress(), result.headOfficeAddress());
        verify(companyRepository).findByIdOrThrow(testCompanyId);
    }

    @Test
    void getCompanyById_WhenCompanyNotExists_ShouldThrowResourceNotFoundException() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(companyRepository.findByIdOrThrow(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Company not found with id: " + nonExistentId));

        // when & then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> companyService.getCompanyById(nonExistentId));
        assertEquals("Company not found with id: " + nonExistentId, exception.getMessage());
        verify(companyRepository).findByIdOrThrow(nonExistentId);
    }

    // --- Тесты для isExistsById ---
    @Test
    void isExistsById_WhenCompanyExists_ShouldReturnTrue() {
        // given
        when(companyRepository.existsById(testCompanyId)).thenReturn(true);

        // when
        Boolean result = companyService.isExistsById(testCompanyId);

        // then
        assertTrue(result);
        verify(companyRepository).existsById(testCompanyId);
    }

    @Test
    void isExistsById_WhenCompanyNotExists_ShouldReturnFalse() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        when(companyRepository.existsById(nonExistentId)).thenReturn(false);

        // when
        Boolean result = companyService.isExistsById(nonExistentId);

        // then
        assertFalse(result);
        verify(companyRepository).existsById(nonExistentId);
    }
}