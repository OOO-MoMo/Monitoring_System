package ru.momo.monitoring.services;

import java.time.Duration;
import java.util.UUID;

public interface RedisService {

    String saveTokenWithValue(String value);

    String getValueByToken(String token);

    void deleteToken(String token);

    void storeRefreshToken(UUID userId, String tokenId, String refreshToken, Duration validityDuration);

    boolean isRefreshTokenValid(String tokenId);

    void invalidateRefreshToken(UUID userId, String tokenId);

    void invalidateAllUserRefreshTokens(UUID userId);

}

