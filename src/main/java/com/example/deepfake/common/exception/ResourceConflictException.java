package com.example.deepfake.common.exception;

public class ResourceConflictException extends RuntimeException {
    
    public ResourceConflictException(String message) {
        super(message);
    }
    
    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
