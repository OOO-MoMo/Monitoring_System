package ru.momo.monitoring.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.momo.monitoring.services.RedisService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;
    private static final long TOKEN_TTL_MINUTES = 60;

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
}

