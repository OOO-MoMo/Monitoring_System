package ru.momo.monitoring.exceptions;

import java.util.function.Supplier;

public class EntityDuplicationException extends RuntimeException {

    public EntityDuplicationException(String message) {
        super(message);
    }

    public EntityDuplicationException(String message, Object... args) {
        super(String.format(message, args));
    }

    public static Supplier<EntityDuplicationException> entityDuplicationExceptionSupplier(String message) {
        return () -> new EntityDuplicationException(message);
    }

    public static Supplier<EntityDuplicationException> entityDuplicationExceptionSupplier(String message, Object... args) {
        return () -> new EntityDuplicationException(message, args);
    }

}
