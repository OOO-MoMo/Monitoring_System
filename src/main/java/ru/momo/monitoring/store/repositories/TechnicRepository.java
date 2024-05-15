package ru.momo.monitoring.store.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.store.entities.Technic;

@Repository
public interface TechnicRepository extends JpaRepository<Technic, Long> {

}
