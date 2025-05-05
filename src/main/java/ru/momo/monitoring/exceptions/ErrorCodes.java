package ru.momo.monitoring.exceptions;

public final class ErrorCodes {
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String AUTHENTICATION_FAILURE = "AUTHENTICATION_FAILURE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String ILLEGAL_STATE = "ILLEGAL_STATE";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String DUPLICATE_ENTITY = "DUPLICATE_ENTITY";
    public static final String SENSOR_CREATION_FAILED = "SENSOR_CREATION_FAILED";
    public static final String SENSOR_BAD_REQUEST = "SENSOR_BAD_REQUEST";
    public static final String USER_BAD_REQUEST = "USER_BAD_REQUEST";

    private ErrorCodes() {
    }
}