package ru.momo.monitoring.store.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.momo.monitoring.exceptions.EntityDuplicationException;
import ru.momo.monitoring.store.entities.Company;

import java.util.Optional;
import java.util.UUID;

import static ru.momo.monitoring.exceptions.ResourceNotFoundException.resourceNotFoundExceptionSupplier;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByInn(String inn);

    Optional<Company> findByName(String name);

    Page<Company> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    default Company findByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(
                resourceNotFoundExceptionSupplier("Company with id = %s is not found", id.toString())
        );
    }

    default void throwIfExistWithSameInn(String inn) {
        findByInn(inn).ifPresent(company -> {
            throw new EntityDuplicationException("Company already exists with inn: " + inn);
        });
    }

    default void throwIfExistWithSameName(String name) {
        findByName(name).ifPresent(company -> {
            throw new EntityDuplicationException("Company already exists with name: " + name);
        });
    }

}
