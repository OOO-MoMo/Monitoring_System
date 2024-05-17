package ru.momo.monitoring.services.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.security.JwtTokenProvider;
import ru.momo.monitoring.services.AuthService;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.store.dto.request.JwtRequest;
import ru.momo.monitoring.store.dto.request.RefreshJwtRequest;
import ru.momo.monitoring.store.dto.request.UserCreateRequestDto;
import ru.momo.monitoring.store.dto.response.JwtResponse;
import ru.momo.monitoring.store.entities.User;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    final AuthenticationManager authenticationManager;
    final UserService userService;
    final JwtTokenProvider jwtTokenProvider;

    @Override
    public JwtResponse login(JwtRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        User user = userService.getByUsername(loginRequest.getUsername());
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse refresh(RefreshJwtRequest refreshToken) {
        return jwtTokenProvider.refreshUserTokens(refreshToken.getRefreshToken());
    }

    @Override
    public JwtResponse register(UserCreateRequestDto registerRequest) {
        User user = userService.getByUsername(userService.create(registerRequest).getUsername());
        return createJwtResponse(user);
    }

    private JwtResponse createJwtResponse(User user){
        return JwtResponse.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .accessToken(jwtTokenProvider.createAccessToken(user.getUserId(), user.getUsername(), user.getRoles()))
                .refreshToken(jwtTokenProvider.createRefreshToken(user.getUserId(), user.getUsername()))
                .build();
    }

}
