package ru.momo.monitoring.services.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.exceptions.UserBadRequestException;
import ru.momo.monitoring.security.JwtTokenProvider;
import ru.momo.monitoring.services.AuthService;
import ru.momo.monitoring.services.CompanyService;
import ru.momo.monitoring.services.EmailService;
import ru.momo.monitoring.services.RedisService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.JwtRequest;
import ru.momo.monitoring.store.dto.request.RefreshJwtRequest;
import ru.momo.monitoring.store.dto.request.auth.RegisterRequest;
import ru.momo.monitoring.store.dto.response.JwtResponse;
import ru.momo.monitoring.store.dto.response.RegisterJwtResponse;
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.UserData;
import ru.momo.monitoring.store.entities.enums.RoleName;
import ru.momo.monitoring.store.entities.enums.UserActionType;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    final AuthenticationManager authenticationManager;
    final UserService userService;
    final JwtTokenProvider jwtTokenProvider;
    final EmailService emailService;
    final RedisService redisService;
    final CompanyService companyService;
    final PasswordEncoder passwordEncoder;

    @Value(value = "${mail.link.confirmation}")
    String linkForConfirm;

    @Override
    public JwtResponse login(JwtRequest loginRequest) {
        User user = userService.getByEmail(loginRequest.getEmail());
        checkActive(user);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword())
        );
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse refresh(RefreshJwtRequest refreshToken) {
        return jwtTokenProvider.refreshUserTokens(refreshToken.getRefreshToken());
    }

    @Override
    public RegisterJwtResponse register(RegisterRequest request, String actorUsername) {
        User existingUser = null;
        try {
            existingUser = userService.getByEmail(request.email());
        } catch (ResourceNotFoundException e) {
            log.info("User with email {} not found. Proceeding with new registration.", request.email());
        }

        Company company = companyService.findById(request.companyId());
        RoleName role = userService.getNewUserRoleByCurrentUser(actorUsername);
        User userToProcess;
        UserActionType actionType;

        if (existingUser != null) {
            if (Boolean.TRUE.equals(existingUser.getIsActive())) {
                throw new UserBadRequestException("User with email " + request.email() + " already exists and is active.");
            } else {
                log.info("User with email {} exists but is inactive. Re-activating/updating.", request.email());
                userToProcess = updateUserForReRegistration(existingUser, request, role, company);
                actionType = UserActionType.RE_REGISTERED_AWAITING_CONFIRMATION;
                redisService.invalidateAllUserRefreshTokens(userToProcess.getId());
            }
        } else {
            log.info("Registering new user with email {}.", request.email());
            userToProcess = userService.saveNotConfirmedUser(request, role, company);
            actionType = UserActionType.REGISTERED_AWAITING_CONFIRMATION;
        }

        String confirmationToken = redisService.saveTokenWithValue(userToProcess.getEmail());
        String apiConfirmUrl = linkForConfirm + confirmationToken;

        String recipientName = (userToProcess.getUserData() != null && userToProcess.getUserData().getFirstname() != null)
                ? userToProcess.getUserData().getFirstname()
                : userToProcess.getEmail();

        String emailHtmlContent = emailService.createConfirmationEmailHtml(recipientName, apiConfirmUrl);

        String emailSubject = actionType == UserActionType.RE_REGISTERED_AWAITING_CONFIRMATION
                ? "Повторное подтверждение регистрации"
                : "Подтверждение регистрации";

        emailService.sendEmail(userToProcess.getEmail(), emailSubject, emailHtmlContent);

        return createRegisterJwtResponse(userToProcess, actionType);
    }

    private User updateUserForReRegistration(User existingUser, RegisterRequest request, RoleName newRole, Company newCompany) {
        if (!request.password().equals(request.passwordConfirmation())) {
            throw new UserBadRequestException("Passwords must match for re-registration.");
        }
        existingUser.setPassword(passwordEncoder.encode(request.password()));
        existingUser.setRole(newRole);
        existingUser.setCompany(newCompany);

        UserData userData = existingUser.getUserData();
        if (userData == null) {
            userData = new UserData();
            userData.setUser(existingUser);
            existingUser.setUserData(userData);
        }
        userData.setFirstname(request.firstname());
        userData.setLastname(request.lastname());
        if (request.phoneNumber() != null) {
            userData.setPhoneNumber(request.phoneNumber());
        }

        existingUser.setIsActive(true);
        existingUser.setIsConfirmed(false);

        return userService.save(existingUser);
    }

    @Override
    public String confirm(String token) {
        String email = redisService.getValueByToken(token);
        if (email == null) {
            throw new UserBadRequestException("Email token is expired");
        }

        userService.confirmUser(email);
        redisService.deleteToken(token);

        return "Email confirmed";
    }

    @Override
    @Transactional
    public String resendConfirmationEmail(String email) {
        User user = userService.getByEmail(email);

        if (Boolean.TRUE.equals(user.getIsConfirmed())) {
            throw new UserBadRequestException("Email is already confirmed.");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UserBadRequestException("Cannot resend confirmation for an inactive account.");
        }

        String confirmationToken = redisService.saveTokenWithValue(email);
        String apiConfirmUrl = linkForConfirm + confirmationToken;
        String recipientName = (user.getUserData() != null && user.getUserData().getFirstname() != null)
                ? user.getUserData().getFirstname()
                : user.getEmail();

        String emailHtmlContent = emailService.createConfirmationEmailHtml(recipientName, apiConfirmUrl);
        emailService.sendEmail(email, "Повторное подтверждение Вашего Email адреса", emailHtmlContent);

        log.info("Resent confirmation email to {}.", email);
        return "Confirmation email has been resent.";
    }

    private JwtResponse createJwtResponse(User user){
        return JwtResponse.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole()))
                .refreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail()))
                .build();
    }

    private RegisterJwtResponse createRegisterJwtResponse(User user, UserActionType actionType) {
        return RegisterJwtResponse.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole()))
                .refreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail()))
                .actionType(actionType)
                .build();
    }

    private void checkActive(User user) {
        if (!user.getIsActive()) {
            throw new UserBadRequestException("User is not active");
        }
    }

}
