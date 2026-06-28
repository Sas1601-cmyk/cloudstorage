package ru.forkin.springcourse.cloudstorage.storage.exceptions;

public class DuplicatedResourceException extends RuntimeException {
    public DuplicatedResourceException(String message) {
        super(message);
    }
}
