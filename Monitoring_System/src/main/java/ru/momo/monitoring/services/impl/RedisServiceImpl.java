package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.RedisService;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;
    private static final long TOKEN_TTL_MINUTES = 60;
    private static final String EMAIL_CONFIRM_TOKEN_PREFIX = "email_confirm:";
    private static final String USER_REFRESH_TOKENS_PREFIX = "user_refresh_tokens:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Override
    public String saveTokenWithValue(String value) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(token, value, TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public String getValueByToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    @Override
    public void deleteToken(String token) {
        redisTemplate.delete(token);
    }

    public void storeRefreshToken(UUID userId, String tokenId, String refreshToken, Duration validityDuration) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + tokenId,
                refreshToken,
                validityDuration
        );

        redisTemplate.opsForSet().add(
                USER_REFRESH_TOKENS_PREFIX + userId.toString(),
                tokenId
        );

        redisTemplate.expire(USER_REFRESH_TOKENS_PREFIX + userId.toString(), validityDuration.plusHours(1));
    }

    public boolean isRefreshTokenValid(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + tokenId));
    }

    public void invalidateRefreshToken(UUID userId, String tokenId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + tokenId);
        if (userId != null) {
            redisTemplate.opsForSet().remove(USER_REFRESH_TOKENS_PREFIX + userId.toString(), tokenId);
        }
    }

    public void invalidateAllUserRefreshTokens(UUID userId) {
        String userTokenSetKey = USER_REFRESH_TOKENS_PREFIX + userId.toString();
        Set<String> tokenIds = redisTemplate.opsForSet().members(userTokenSetKey);
        if (tokenIds != null && !tokenIds.isEmpty()) {
            List<String> tokenKeysToDelete = tokenIds.stream()
                    .map(tokenId -> REFRESH_TOKEN_PREFIX + tokenId)
                    .collect(Collectors.toList());
            if (!tokenKeysToDelete.isEmpty()) {
                redisTemplate.delete(tokenKeysToDelete);
            }
        }
        redisTemplate.delete(userTokenSetKey);
    }
}

