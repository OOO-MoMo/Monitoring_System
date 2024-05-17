package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.exceptions.user.UserBadRequestException;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.response.UserCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserUpdateResponseDto;
import ru.momo.monitoring.store.entities.Role;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.repositories.UserDataRepository;
import ru.momo.monitoring.store.repositories.UserRepository;

import java.util.Objects;
import java.util.Set;

import static ru.momo.monitoring.exceptions.user.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;

    @Override
    public UserResponseDto getById(Long id) {
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
    public User getByIdEntity(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with id = %d is not found", id)
                );
    }

    @Override
    public User getByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier("User with username = %s is not found", username)
                );
    }

    @Override
    public UserCreatedResponseDto create(UserCreateRequestDto request) {
        User user = UserCreateRequestDto.mapToUserEntity(request);

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserBadRequestException("User with name %s already exists", user.getUsername());
        }

        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new UserBadRequestException("Password and password confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = Set.of(new Role(2L, "ROLE_USER"));
        user.setRoles(roles);
        UserData data = UserCreateRequestDto.mapToUserDataEntity(request);
        user.setUserData(data);
        data.setUser(user);
        userRepository.save(user);
        return UserCreatedResponseDto.MapFromEntity(user, data);
    }

    /*Пока что изменяю только username
    В будущем можно изменять и другие поля тоже*/
    @Override
    public UserUpdateResponseDto update(UserUpdateRequestDto request) {
        if (Objects.equals(request.getOldUsername(), request.getNewUsername())) {
            throw new UserBadRequestException("You already have this username");
        }

        User updatedUser = userRepository
                .findByUsername(request.getOldUsername())
                .orElseThrow(
                        resourceNotFoundExceptionSupplier(
                                "User with username = %s is not exist", request.getOldUsername()
                        )
                );

        if (request.getNewUsername() != null) {
            if (userRepository.findByUsername(request.getNewUsername()).isPresent()) {
                throw new UserBadRequestException("User with name %s already exists", request.getNewUsername());
            }
            updatedUser.setUsername(request.getNewUsername());
        }

        userRepository.save(updatedUser);

        return UserUpdateResponseDto.mapFromEntity(updatedUser);
    }

    @Override
    public void delete(Long id) {
        User deletedUser = userRepository
                .findById(id)
                .orElseThrow(
                        resourceNotFoundExceptionSupplier(
                                "User with id = %d is not exist", id
                        )
                );
        userRepository.delete(deletedUser);
    }

}
