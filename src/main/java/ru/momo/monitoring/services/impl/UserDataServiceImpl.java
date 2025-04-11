package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.UserDataService;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.repositories.UserDataRepository;

@Service
@RequiredArgsConstructor
public class UserDataServiceImpl implements UserDataService {

    private final UserDataRepository userDataRepository;

    @Override
    public void saveUserData(UserData userData) {
        userDataRepository.save(userData);
    }
}
