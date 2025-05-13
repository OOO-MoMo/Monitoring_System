package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.AccessDeniedException;
import ru.momo.monitoring.exceptions.UserBadRequestException;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.UserUpdateRequestDto;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.CompanyIdResponseDto;
import ru.momo.monitoring.store.dto.response.UserResponseDto;
import ru.momo.monitoring.store.dto.response.UserRoleResponseDto;
import ru.momo.monitoring.store.dto.response.UsersResponseDto;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.repositories.UserRepository;
import ru.momo.monitoring.store.repositories.specification.UserSpecifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        User user = userRepository.findByIdOrThrow(id);
        return UserResponseDto.mapFromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByIdEntity(UUID id) {
        return userRepository.findByIdOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailOrThrow(email);
    }

    @Override
    @Transactional
    public UserResponseDto update(UserUpdateRequestDto request, String email) {
        User user = userRepository.findByEmailOrThrow(email);

        if (user.getUserData() == null) {
            UserData newUserData = new UserData();
            newUserData.setUser(user);
            user.setUserData(newUserData);
        }

        UserData userData = user.getUserData();

        updateUserInfo(request, userData);

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (userRepository.existsByUserData_PhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Номер телефона уже используется");
            }
            userData.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);

        return UserResponseDto.mapFromEntity(user);
    }

    //todo надо сделать чтобы у пользователя токены становились не валидными
    @Override
    @Transactional
    public void delete(UUID id) {
        User deletedUser = userRepository.findByIdOrThrow(id);
        deletedUser.setIsActive(false);
        userRepository.save(deletedUser);
    }

    @Override
    @Transactional
    public User confirmUser(String email) {
        User user = getByEmail(email);
        user.setIsConfirmed(true);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleName getNewUserRoleByCurrentUser(String username) {
        User user = getByEmail(username);
        RoleName currentUserRole = user.getRole();

        return switch (currentUserRole) {
            case ROLE_ADMIN -> RoleName.ROLE_MANAGER;
            case ROLE_MANAGER -> RoleName.ROLE_DRIVER;
            case ROLE_DRIVER -> throw new IllegalStateException("Водитель не может регистрировать пользователей");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserByEmail(String email) {
        User user = getByEmail(email);
        return UserResponseDto.mapFromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleResponseDto getCurrentUserRoleByEmail(String email) {
        User user = getByEmail(email);
        return new UserRoleResponseDto(user.getRole().name());
    }

    @Override
    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllActiveByCompanyId(UUID id) {
        return userRepository.findUserByCompany_Id(id);
    }

    @Override
    public UsersResponseDto searchActiveDrivers(
            String firstname,
            String lastname,
            String patronymic,
            String managerEmail
    ) {
        User manager = getByEmail(managerEmail);
        String organization = manager.getCompany().getName();

        Specification<User> spec = Specification.where(UserSpecifications.isActiveAndConfirmedDriver());

        spec = updateSpecificationByInitials(firstname, lastname, patronymic, spec);

        if (organization != null && !organization.isBlank()) {
            spec = spec.and(UserSpecifications.hasOrganizationByName(organization));
        }

        List<User> users = getUsersBySpecification(spec);

        List<UserResponseDto> result = users.stream().map(UserResponseDto::mapFromEntity).toList();

        return new UsersResponseDto(result);
    }

    @Override
    public CompanyIdResponseDto getCompanyIdForManager(String email) {
        User manager = getByEmail(email);

        return new CompanyIdResponseDto(manager.getCompany().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public UsersResponseDto searchManagers(
            UUID companyId,
            String firstname,
            String lastname,
            String patronymic,
            Boolean isActive
    ) {
        Specification<User> spec = Specification.where(UserSpecifications.isActiveAndConfirmedManager(isActive));

        return getUsersResponseDto(companyId, firstname, lastname, patronymic, spec);
    }

    @Override
    @Transactional(readOnly = true)
    public UsersResponseDto searchDrivers(
            UUID companyId,
            String firstname,
            String lastname,
            String patronymic,
            Boolean isActive
    ) {
        Specification<User> spec = Specification.where(UserSpecifications.isActiveAndConfirmedDriver(isActive));

        return getUsersResponseDto(companyId, firstname, lastname, patronymic, spec);
    }

    @Override
    @Transactional
    public UserResponseDto updateById(UUID userId, UserUpdateRequestDto request, String actorEmail) {
        User userToUpdate = userRepository.findByIdOrThrow(userId);

        User actor = userRepository.findByEmailOrThrow(actorEmail);

        if (actor.getRole() == RoleName.ROLE_MANAGER) {
            if (userToUpdate.getCompany() == null || actor.getCompany() == null ||
                    !userToUpdate.getCompany().getId().equals(actor.getCompany().getId())) {
                throw new AccessDeniedException("Manager can only update users within their own company.");
            }
        } else if (actor.getRole() != RoleName.ROLE_ADMIN) {
            throw new AccessDeniedException("Only Admins or Managers can update users by ID.");
        }

        UserData userData = userToUpdate.getUserData();
        if (userData == null) {
            userData = new UserData();
            userData.setUser(userToUpdate);
            userToUpdate.setUserData(userData);
        }

        updateUserInfo(request, userData);

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            String newPhoneNumber = request.getPhoneNumber().trim();
            if (userRepository.existsByUserData_PhoneNumberAndIdNot(newPhoneNumber, userToUpdate.getId())) {
                throw new UserBadRequestException("Этот номер телефона уже используется другим пользователем.");
            }
            userData.setPhoneNumber(newPhoneNumber);
        } else if (request.getPhoneNumber() != null && request.getPhoneNumber().isBlank()) {
            userData.setPhoneNumber(null);
        }

        User updatedUser = userRepository.save(userToUpdate);

        return UserResponseDto.mapFromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateDriverByManager(UUID driverId, String managerEmail) {
        User manager = getByEmail(managerEmail);

        User driverToDeactivate = userRepository.findByIdOrThrow(driverId);

        if (driverToDeactivate.getRole() != RoleName.ROLE_DRIVER) {
            throw new UserBadRequestException("User with id " + driverId + " is not a driver.");
        }

        if (manager.getCompany() == null || driverToDeactivate.getCompany() == null ||
                !manager.getCompany().getId().equals(driverToDeactivate.getCompany().getId())) {
            throw new AccessDeniedException("Manager can only deactivate drivers within their own company.");
        }

        // todo: Также нужно инвалидировать токены водителя (как и в методе delete)
        driverToDeactivate.setIsActive(false);
        userRepository.save(driverToDeactivate);
    }

    @Override
    @Transactional
    public User saveNotConfirmedUser(RegisterRequest request, RoleName role, Company company) {
        if (!request.password().equals(request.passwordConfirmation())) {
            throw new UserBadRequestException("Пароли должны совпадать");
        }

        User user = new User();
        UserData userData = new UserData();
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setCompany(company);
        userData.setFirstname(request.firstname());
        userData.setLastname(request.lastname());
        userData.setPhoneNumber(request.phoneNumber());
        user.setUserData(userData);
        userData.setUser(user);
        company.addUser(user);

        if (role.equals(RoleName.ROLE_DRIVER)) {
            user.setTechnics(new ArrayList<>());
        }

        return userRepository.save(user);
    }

    private <T> void updateField(T value, Consumer<T> setter) {
        Optional.ofNullable(value)
                .filter(v -> !(v instanceof String) || !((String) v).isBlank())
                .ifPresent(setter);
    }

    private void updateUserInfo(UserUpdateRequestDto request, UserData userData) {
        updateField(request.getFirstname(), userData::setFirstname);
        updateField(request.getLastname(), userData::setLastname);
        updateField(request.getPatronymic(), userData::setPatronymic);
        updateField(request.getDateOfBirth(), userData::setDateOfBirth);
        updateField(request.getAddress(), userData::setAddress);
    }

    private List<User> getUsersBySpecification(Specification<User> spec) {
        List<User> users = userRepository.findAll(spec, Sort.by(
                Sort.Order.asc("userData.firstname"),
                Sort.Order.asc("userData.lastname"),
                Sort.Order.asc("userData.patronymic"),
                Sort.Order.asc("company.name")
        ));
        return users;
    }

    private Specification<User> updateSpecificationByInitials(
            String firstname,
            String lastname,
            String patronymic,
            Specification<User> spec
    ) {
        if (firstname != null && !firstname.isBlank()) {
            spec = spec.and(UserSpecifications.hasFirstname(firstname));
        }

        if (lastname != null && !lastname.isBlank()) {
            spec = spec.and(UserSpecifications.hasLastname(lastname));
        }

        if (patronymic != null && !patronymic.isBlank()) {
            spec = spec.and(UserSpecifications.hasPatronymic(patronymic));
        }

        return spec;
    }

    private UsersResponseDto getUsersResponseDto(
            UUID companyId,
            String firstname,
            String lastname,
            String patronymic,
            Specification<User> spec
    ) {
        spec = spec.and(UserSpecifications.hasOrganizationById(companyId));

        updateSpecificationByInitials(firstname, lastname, patronymic, spec);

        List<User> users = getUsersBySpecification(spec);

        List<UserResponseDto> result = users.stream().map(UserResponseDto::mapFromEntity).toList();

        return new UsersResponseDto(result);
    }

}
