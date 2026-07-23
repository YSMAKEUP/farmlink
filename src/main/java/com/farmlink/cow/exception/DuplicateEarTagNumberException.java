package com.farmlink.cow.exception;

public class DuplicateEarTagNumberException extends RuntimeException {
    public DuplicateEarTagNumberException(String message) {
        super(message);
    }
}