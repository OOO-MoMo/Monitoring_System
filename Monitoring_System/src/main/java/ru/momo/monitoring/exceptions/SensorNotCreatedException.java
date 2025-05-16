package ru.momo.monitoring.exceptions;

public class SensorNotCreatedException extends RuntimeException {

    public SensorNotCreatedException(String message) {
        super(message);
    }

    public SensorNotCreatedException(String message, Object... args) {
        super(String.format(message, args));
    }

}
