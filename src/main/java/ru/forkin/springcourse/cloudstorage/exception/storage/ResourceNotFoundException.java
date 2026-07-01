package ru.forkin.springcourse.cloudstorage.exception.storage;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
