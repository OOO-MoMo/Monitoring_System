package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;

import java.util.Optional;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Long> {

    Optional<UserData> findByUser(User user);

}
