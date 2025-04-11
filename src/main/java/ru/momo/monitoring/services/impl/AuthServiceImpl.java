package ru.momo.monitoring.services.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
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
import ru.momo.monitoring.store.entities.Company;
import ru.momo.monitoring.store.entities.User;
import ru.momo.monitoring.store.entities.enums.RoleName;

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
    public JwtResponse register(RegisterRequest request, String username) {
        String token = redisService.saveTokenWithValue(request.email());
        emailService.sendEmail(request.email(), token);

        Company company = companyService.findById(request.companyId());
        RoleName role = userService.getNewUserRoleByCurrentUser(username);
        User user = userService.saveNotConfirmedUser(request, role, company);

        return createJwtResponse(user);
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
    public String resendConfirmationEmail(String email) {
        User user = userService.getByEmail(email);

        if (user.getIsConfirmed()) {
            throw new UserBadRequestException("Email is already confirmed");
        }

        String token = redisService.saveTokenWithValue(email);
        emailService.sendEmail(email, token);

        return "Email resent";
    }

    private JwtResponse createJwtResponse(User user){
        return JwtResponse.builder()
                .id(user.getId())
                .username(user.getEmail())
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole()))
                .refreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail()))
                .build();
    }

    private void checkActive(User user) {
        if (!user.getIsActive()) {
            throw new UserBadRequestException("User is not active");
        }
    }

}
