package com.bcm.service.error;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
