package ru.forkin.springcourse.cloudstorage.exception.storage;

public class InvalidPathException extends RuntimeException {
    public InvalidPathException(String message) {
        super(message);
    }
}
