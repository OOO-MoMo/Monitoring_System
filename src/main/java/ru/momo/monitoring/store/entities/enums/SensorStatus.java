package ru.momo.monitoring.store.entities.enums;

public enum SensorStatus {
    NORMAL,    // Значение в допустимом диапазоне
    WARNING,   // Приближается к границам диапазона
    CRITICAL   // Вышло за допустимые пределы
}