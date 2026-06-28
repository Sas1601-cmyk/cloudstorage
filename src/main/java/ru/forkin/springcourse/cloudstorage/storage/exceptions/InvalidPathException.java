package ru.forkin.springcourse.cloudstorage.storage.exceptions;

public class InvalidPathException extends RuntimeException {
    public InvalidPathException(String message) {
        super(message);
    }
}
