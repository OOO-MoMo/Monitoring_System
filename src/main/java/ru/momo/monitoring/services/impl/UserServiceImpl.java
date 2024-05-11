package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.UserResponseDto;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.repositories.UserDataRepository;
import ru.momo.monitoring.store.repositories.UserRepository;

import static ru.momo.monitoring.exceptions.user.UserNotFoundByIdException.userNotFoundByIdExceptionSupplier;

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

}
