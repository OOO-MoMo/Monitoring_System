package ru.momo.monitoring.services;

public interface RedisService {
    String saveTokenWithValue(String value);

    String getValueByToken(String token);

    void deleteToken(String token);
}

