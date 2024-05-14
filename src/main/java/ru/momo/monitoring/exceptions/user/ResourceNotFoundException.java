package ru.momo.monitoring.exceptions.user;

import java.util.function.Supplier;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }

    public static Supplier<ResourceNotFoundException> resourceNotFoundExceptionSupplier(String message) {
        return () -> new ResourceNotFoundException(message);
    }

    public static Supplier<ResourceNotFoundException> resourceNotFoundExceptionSupplier(String message, Object... args) {
        return () -> new ResourceNotFoundException(message, args);
    }

}
