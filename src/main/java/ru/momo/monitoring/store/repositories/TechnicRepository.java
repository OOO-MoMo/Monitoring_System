package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.store.entities.Technic;
import ru.momo.monitoring.store.entities.User;

import java.util.Optional;

@Repository
public interface TechnicRepository extends JpaRepository<Technic, Long> {
    Optional<Technic> findByOwnerId(User owner);

}
