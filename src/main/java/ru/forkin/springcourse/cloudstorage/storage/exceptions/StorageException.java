package ru.forkin.springcourse.cloudstorage.storage.exceptions;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
}
