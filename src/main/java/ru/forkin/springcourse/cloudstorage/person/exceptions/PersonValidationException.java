package ru.forkin.springcourse.cloudstorage.person.exceptions;

public class PersonValidationException extends RuntimeException{
    public PersonValidationException(String message) {
        super(message);
    }
}
