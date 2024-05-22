package ru.momo.monitoring.exceptions.user;

import java.util.function.Supplier;

public class SensorBadRequestException extends RuntimeException {

    public SensorBadRequestException(String message) {
        super(message);
    }

    public SensorBadRequestException(String message, Object... args){
        super(String.format(message, args));
    }

    public static Supplier<SensorBadRequestException> sensorBadRequestExceptionSupplier(String message){
        return () -> new SensorBadRequestException(message);
    }

    public static Supplier<SensorBadRequestException> sensorBadRequestExceptionSupplier(String message, Object... args){
        return () -> new SensorBadRequestException(message, args);
    }

}
