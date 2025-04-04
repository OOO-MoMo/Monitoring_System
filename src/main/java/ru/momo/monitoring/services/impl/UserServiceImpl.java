package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.UserBadRequestException;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserRoleResponseDto;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        User user = userRepository.findByIdOrThrow(id);
        return UserResponseDto.mapFromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByIdEntity(UUID id) {
        return userRepository.findByIdOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailOrThrow(email);
    }

    @Override
    @Transactional
    public UserResponseDto update(UserUpdateRequestDto request, String email) {
        User user = userRepository.findByEmailOrThrow(email);
        UserData userData = user.getUserData();

        updateField(request.getFirstname(), userData::setFirstname);
        updateField(request.getLastname(), userData::setLastname);
        updateField(request.getPatronymic(), userData::setPatronymic);
        updateField(request.getDateOfBirth(), userData::setDateOfBirth);
        updateField(request.getAddress(), userData::setAddress);
        updateField(request.getOrganization(), userData::setOrganization);

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (userRepository.existsByUserData_PhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Номер телефона уже используется");
            }
            userData.setPhoneNumber(request.getPhoneNumber());
        }

        return UserResponseDto.mapFromEntity(user);
    }

    //todo надо сделать чтобы у пользователя токены становились не валидными
    @Override
    @Transactional
    public void delete(UUID id) {
        User deletedUser = userRepository.findByIdOrThrow(id);
        deletedUser.setIsActive(false);
        userRepository.save(deletedUser);
    }

    @Override
    @Transactional
    public User confirmUser(String email) {
        User user = getByEmail(email);
        user.setIsConfirmed(true);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleName getNewUserRoleByCurrentUser(String username) {
        User user = getByEmail(username);
        RoleName currentUserRole = user.getRole();

        return switch (currentUserRole) {
            case ROLE_ADMIN -> RoleName.ROLE_MANAGER;
            case ROLE_MANAGER -> RoleName.ROLE_DRIVER;
            case ROLE_DRIVER -> throw new IllegalStateException("Водитель не может регистрировать пользователей");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserByEmail(String email) {
        User user = getByEmail(email);
        return UserResponseDto.mapFromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleResponseDto getCurrentUserRoleByEmail(String email) {
        User user = getByEmail(email);
        return new UserRoleResponseDto(user.getRole().name());
    }

    @Override
    @Transactional
    public User saveNotConfirmedUser(RegisterRequest request, RoleName role) {
        if (!request.password().equals(request.passwordConfirmation())) {
            throw new UserBadRequestException("Пароли должны совпадать");
        }

        User user = new User();
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());

        if (role.equals(RoleName.ROLE_DRIVER)) {
            user.setTechnics(new ArrayList<>());
        }

        return userRepository.save(user);
    }

    private <T> void updateField(T value, Consumer<T> setter) {
        Optional.ofNullable(value)
                .filter(v -> !(v instanceof String) || !((String) v).isBlank())
                .ifPresent(setter);
    }

}
