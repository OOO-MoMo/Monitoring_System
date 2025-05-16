package ru.momo.monitoring.store.repositories.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.momo.monitoring.store.entities.Technic;

import java.util.UUID;

public class TechnicSpecification {

    public static Specification<Technic> filterTechnics(
            UUID companyId,
            UUID ownerId,
            Integer year,
            String brand,
            String model,
            Boolean isActive
    ) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (companyId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("company").get("id"), companyId));
            }

            if (ownerId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("ownerId").get("id"), ownerId));
            }

            if (year != null) {
                predicate = cb.and(predicate, cb.equal(root.get("year"), year));
            }

            if (brand != null && !brand.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("brand")), "%" + brand.toLowerCase() + "%"));
            }

            if (model != null && !model.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("model")), "%" + model.toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicate = cb.and(predicate, cb.equal(root.get("isActive"), isActive));
            }

            return predicate;
        };
    }

}
