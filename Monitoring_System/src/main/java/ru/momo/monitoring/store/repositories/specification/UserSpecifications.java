package ru.momo.monitoring.store.repositories.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;

import java.util.UUID;

public class UserSpecifications {

    public static Specification<User> isActiveAndConfirmedDriver() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), true),
                cb.equal(root.get("isConfirmed"), true),
                cb.equal(root.get("role"), RoleName.ROLE_DRIVER)
        );
    }

    public static Specification<User> isActiveAndConfirmedDriver(Boolean isActive) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), isActive),
                cb.equal(root.get("isConfirmed"), true),
                cb.equal(root.get("role"), RoleName.ROLE_DRIVER)
        );
    }

    public static Specification<User> isActiveAndConfirmedManager(Boolean isActive) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("isActive"), isActive),
                cb.equal(root.get("isConfirmed"), true),
                cb.equal(root.get("role"), RoleName.ROLE_MANAGER)
        );
    }

    public static Specification<User> hasFirstname(String firstname) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("userData").get("firstname")),
                "%" + firstname.toLowerCase() + "%"
        );
    }

    public static Specification<User> hasLastname(String lastname) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("userData").get("lastname")),
                "%" + lastname.toLowerCase() + "%"
        );
    }

    public static Specification<User> hasPatronymic(String patronymic) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("userData").get("patronymic")),
                "%" + patronymic.toLowerCase() + "%"
        );
    }

    public static Specification<User> hasOrganizationByName(String organization) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("company").get("name")), "%" + organization.toLowerCase() + "%"
        );
    }

    public static Specification<User> hasOrganizationById(UUID organizationId) {
        return (root, query, cb) -> cb.equal(
                root.join("company").get("id"),
                organizationId
        );
    }
}
