package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.exceptions.user.UserBadRequestException;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.response.UserCreatedResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.repositories.UserDataRepository;
import ru.momo.monitoring.store.repositories.UserRepository;

import static ru.momo.monitoring.exceptions.user.UserBadRequestException.userNotFoundByIdExceptionSupplier;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDataRepository userDataRepository;

    @Override
    public UserResponseDto getById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(
                        userNotFoundByIdExceptionSupplier("User with id = %d is not found", id)
                );

        //user и так существует, поэтому сразу вызываю метод get()
        UserData data = userDataRepository.findByUser(user).get();

        return UserResponseDto.mapFromEntity(user, data);
    }

    @Override
    public UserCreatedResponseDto create(UserCreateRequestDto request) {
        User user = UserCreateRequestDto.mapToUserEntity(request);

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserBadRequestException("User with name %s already exists", user.getUsername());
        }

        if(!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new UserBadRequestException("Password and password confirmation do not match");
        }

        UserData data = UserCreateRequestDto.mapToUserDataEntity(request);
        data.setUser(user);
        userRepository.save(user);
        userDataRepository.save(data);
        return UserCreatedResponseDto.MapFromEntity(user, data);
    }

}
