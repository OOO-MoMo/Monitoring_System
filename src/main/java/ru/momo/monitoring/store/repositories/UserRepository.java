package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.store.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
