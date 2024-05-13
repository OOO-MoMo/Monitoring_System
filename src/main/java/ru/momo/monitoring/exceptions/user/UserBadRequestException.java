package ru.momo.monitoring.exceptions.user;

import java.util.function.Supplier;

public class UserBadRequestException extends RuntimeException{

    public UserBadRequestException(String message) {
        super(message);
    }

    public UserBadRequestException(String message, Object... args){
        super(String.format(message, args));
    }

    public static Supplier<UserBadRequestException> userBadRequestExceptionSupplier(String message){
        return () -> new UserBadRequestException(message);
    }

    public static Supplier<UserBadRequestException> userBadRequestExceptionSupplier(String message, Object... args){
        return () -> new UserBadRequestException(message, args);
    }

}
