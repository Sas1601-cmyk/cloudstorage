package ru.forkin.springcourse.cloudstorage.exception.storage;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
}
