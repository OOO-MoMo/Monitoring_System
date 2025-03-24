package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.UserBadRequestException;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.UserCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserUpdateResponseDto;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.repositories.UserDataRepository;
import ru.momo.monitoring.store.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static ru.momo.monitoring.exceptions.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with id = %d is not found", id)
                );

        //user и так существует, поэтому сразу вызываю метод get()
        UserData data = userDataRepository.findByUser(user).get();

        return UserResponseDto.mapFromEntity(user, data);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByIdEntity(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with id = %d is not found", id)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with email = %s is not found", email)
                );
    }

    @Override
    @Transactional
    public UserCreatedResponseDto create(UserCreateRequestDto request) {
        User user = UserCreateRequestDto.mapToUserEntity(request);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserBadRequestException("User with email %s already exists", user.getEmail());
        }

        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new UserBadRequestException("Password and password confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleName.ROLE_ADMIN);
        UserData data = UserCreateRequestDto.mapToUserDataEntity(request);
        user.setUserData(data);
        data.setUser(user);
        userRepository.save(user);
        return UserCreatedResponseDto.mapFromEntity(user, data);
    }

    /*Пока что изменяю только username
    В будущем можно изменять и другие поля тоже*/
    @Override
    @Transactional
    public UserUpdateResponseDto update(UserUpdateRequestDto request) {
        if (Objects.equals(request.getOldUsername(), request.getNewUsername())) {
            throw new UserBadRequestException("You already have this username");
        }

        User updatedUser = userRepository
                .findByEmail(request.getOldUsername())
                .orElseThrow(
                        resourceNotFoundExceptionSupplier(
                                "User with username = %s is not exist", request.getOldUsername()
                        )
                );

        if (request.getNewUsername() != null) {
            if (userRepository.findByEmail(request.getNewUsername()).isPresent()) {
                throw new UserBadRequestException("User with name %s already exists", request.getNewUsername());
            }
            updatedUser.setEmail(request.getNewUsername());
        }

        userRepository.save(updatedUser);

        return UserUpdateResponseDto.mapFromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User deletedUser = userRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier(
                                "User with id = %d is not exist", id
                        )
                );
        userRepository.delete(deletedUser);
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

}
