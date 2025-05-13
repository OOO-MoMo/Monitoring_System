package ru.momo.monitoring.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ru.momo.monitoring.services.impl.RedisServiceImpl;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private RedisServiceImpl redisService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void saveTokenWithValue_ShouldStoreTokenAndReturnIt() {
        String value = "someValue";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = redisService.saveTokenWithValue(value);

        assertNotNull(token);
        verify(redisTemplate.opsForValue()).set(eq(token), eq(value), eq(60L), eq(TimeUnit.MINUTES));
    }

    @Test
    void getValueByToken_ShouldReturnStoredValue() {
        String token = "test-token";
        String expectedValue = "stored-value";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(token)).thenReturn(expectedValue);

        String result = redisService.getValueByToken(token);

        assertEquals(expectedValue, result);
        verify(valueOperations).get(token);
    }

    @Test
    void deleteToken_ShouldCallRedisDelete() {
        String token = "test-token";

        redisService.deleteToken(token);

        verify(redisTemplate).delete(token);
    }
}
