package ru.momo.monitoring.exceptions.user;

import java.util.function.Supplier;

public class UserNotFoundByIdException extends RuntimeException{

    public UserNotFoundByIdException(String message) {
        super(message);
    }

    public UserNotFoundByIdException(String message, Object... args){
        super(String.format(message, args));
    }

    public static Supplier<UserNotFoundByIdException> userNotFoundByIdExceptionSupplier(String message){
        return () -> new UserNotFoundByIdException(message);
    }

    public static Supplier<UserNotFoundByIdException> userNotFoundByIdExceptionSupplier(String message, Object... args){
        return () -> new UserNotFoundByIdException(message, args);
    }

}
