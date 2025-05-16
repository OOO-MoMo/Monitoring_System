package ru.momo.monitoring.services;

import ru.momo.monitoring.store.entities.User;

public interface SecurityService {

    User getCurrentUser();

}
