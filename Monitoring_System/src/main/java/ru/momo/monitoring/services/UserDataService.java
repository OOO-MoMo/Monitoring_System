package ru.momo.monitoring.services;

import ru.momo.monitoring.store.entities.UserData;

public interface UserDataService {

    void saveUserData(UserData userData);

}
