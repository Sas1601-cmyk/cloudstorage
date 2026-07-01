package ru.forkin.springcourse.cloudstorage.exception.storage;

public class DuplicatedResourceException extends RuntimeException {
    public DuplicatedResourceException(String message) {
        super(message);
    }
}
