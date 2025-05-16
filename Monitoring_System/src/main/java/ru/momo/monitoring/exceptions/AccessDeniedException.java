package ru.momo.monitoring.exceptions;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {
        super();
    }

    public AccessDeniedException(String message) {
        super(message);
    }

}
