package org.assignment.exceptions;

public class AvailabilityException extends RuntimeException {
    public AvailabilityException(String message) {
        super(message);
    }

    public AvailabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}


