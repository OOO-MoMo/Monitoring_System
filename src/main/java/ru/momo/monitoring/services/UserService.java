package ru.momo.monitoring.services;

import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.ActiveDriversResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserRoleResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDto getById(UUID id);

    User getByIdEntity(UUID id);

    User getByEmail(String email);

    UserResponseDto update(UserUpdateRequestDto request, String email);

    void delete(UUID id);

    User saveNotConfirmedUser(RegisterRequest request, RoleName role, Company company);

    User confirmUser(String email);

    RoleName getNewUserRoleByCurrentUser(String username);

    UserResponseDto getCurrentUserByEmail(String email);

    UserRoleResponseDto getCurrentUserRoleByEmail(String email);

    void save(User user);

    List<User> findAllActiveByCompanyId(UUID id);

    ActiveDriversResponseDto searchActiveDrivers(
            String firstname,
            String lastname,
            String patronymic,
            String managerEmail
    );

}
