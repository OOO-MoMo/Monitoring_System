package ru.momo.monitoring.store.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.momo.monitoring.store.entities.Technic;

import java.util.UUID;

@Repository
public interface TechnicRepository extends JpaRepository<Technic, UUID> {

    Page<Technic> findAllByOwnerIdIdAndBrandContainingIgnoreCaseAndModelContainingIgnoreCase(
            UUID ownerId,
            String brand,
            String model,
            Pageable pageable
    );

}
