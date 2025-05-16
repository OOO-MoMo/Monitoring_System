package ru.momo.monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.services.impl.CompanyServiceImpl;
import ru.momo.monitoring.store.dto.request.CompanyCreateRequestDto;
import ru.momo.monitoring.store.dto.request.CompanyUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.CompanyResponseDto;
import ru.momo.monitoring.store.dto.response.PageCompanyResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.repositories.CompanyRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

    @Test
    void create_ShouldSaveCompanyAndReturnDto() {
        // given
        CompanyCreateRequestDto request = new CompanyCreateRequestDto("ООО Ромашка", "770708389333", "г. Москва, ул. Ленина, д. 1");

        Company company = Company.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .inn(request.inn())
                .headOfficeAddress(request.headOfficeAddress())
                .build();

        // stub: не выбрасываем исключения в validate
        doNothing().when(companyRepository).throwIfExistWithSameInn(request.inn());
        doNothing().when(companyRepository).throwIfExistWithSameName(request.name());

        when(companyRepository.save(any(Company.class))).thenReturn(company);

        // when
        CompanyResponseDto result = companyService.create(request);

        // then
        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.inn(), result.inn());
        assertEquals(request.headOfficeAddress(), result.headOfficeAddress());

        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void delete_ShouldDeactivateUsersAndDeleteCompany() {
        // given
        UUID id = UUID.randomUUID();
        Company company = new Company();
        company.setName("Test Company");
        company.setInn("123456789012");
        company.setHeadOfficeAddress("Москва");

        User user1 = new User();
        user1.setIsActive(true);

        User user2 = new User();
        user2.setIsActive(true);

        List<User> users = List.of(user1, user2);

        when(companyRepository.findByIdOrThrow(id)).thenReturn(company);
        when(userService.findAllActiveByCompanyId(id)).thenReturn(users);

        // when
        companyService.delete(id);

        // then
        verify(userService, times(2)).save(any(User.class));
        verify(companyRepository).delete(company);
    }

    @Test
    void update_ShouldUpdateCompanyFields() {
        // given
        UUID companyId = UUID.randomUUID();
        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto(
                companyId,
                "Обновленное имя",
                "123456789012",
                "Новый адрес"
        );

        Company company = new Company();
        company.setName("Старое имя");
        company.setInn("000000000000");
        company.setHeadOfficeAddress("Старый адрес");

        when(companyRepository.findByIdOrThrow(companyId)).thenReturn(company);

        // do nothing для проверок уникальности
        doNothing().when(companyRepository).throwIfExistWithSameInn(request.inn());
        doNothing().when(companyRepository).throwIfExistWithSameName(request.name());

        when(companyRepository.save(any(Company.class))).thenReturn(company);

        // when
        CompanyResponseDto result = companyService.update(request);

        // then
        assertEquals(request.name(), result.name());
        assertEquals(request.inn(), result.inn());
        assertEquals(request.headOfficeAddress(), result.headOfficeAddress());

        verify(companyRepository).save(company);
    }

    @Test
    void findById_ShouldReturnCompany() {
        // given
        UUID companyId = UUID.randomUUID();
        Company company = new Company();
        company.setName("Test Company");
        company.setInn("123456789012");
        company.setHeadOfficeAddress("Москва");

        when(companyRepository.findByIdOrThrow(companyId)).thenReturn(company);

        // when
        Company result = companyService.findById(companyId);

        // then
        assertEquals("Test Company", result.getName());
        verify(companyRepository).findByIdOrThrow(companyId);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        // given
        Company company = new Company();
        company.setName("Новая компания");

        // when
        companyService.save(company);

        // then
        verify(companyRepository).save(company);
    }

    @Test
    void findAll_ShouldReturnPagedCompanyResponseDtos() {
        // given
        Pageable pageable = PageRequest.of(0, 2);
        Company company1 = new Company();
        company1.setName("Ромашка");
        company1.setInn("123456789012");
        company1.setHeadOfficeAddress("Москва");

        Company company2 = new Company();
        company2.setName("Одуванчик");
        company2.setInn("987654321098");
        company2.setHeadOfficeAddress("Питер");

        Page<Company> page = new PageImpl<>(List.of(company1, company2), pageable, 2);

        when(companyRepository.findAllByNameContainingIgnoreCase("", pageable)).thenReturn(page);

        // when
        PageCompanyResponseDto result = companyService.findAll(null, pageable);

        // then
        assertEquals(2, result.content().size());
        assertEquals("Ромашка", result.content().get(0).name());
        assertEquals("Одуванчик", result.content().get(1).name());

        verify(companyRepository).findAllByNameContainingIgnoreCase("", pageable);
    }

}
