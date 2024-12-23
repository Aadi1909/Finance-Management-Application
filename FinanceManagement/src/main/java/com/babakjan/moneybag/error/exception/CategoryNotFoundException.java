package com.babakjan.moneybag.error.exception;

public class CategoryNotFoundException extends Exception {
    public CategoryNotFoundException() {
    }

    public CategoryNotFoundException(Long id) {
        super("Category of id: " + id + " not found.");
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoryNotFoundException(Throwable cause) {
        super(cause);
    }

    public CategoryNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
