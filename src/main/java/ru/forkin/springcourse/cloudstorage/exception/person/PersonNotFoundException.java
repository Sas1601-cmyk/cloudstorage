package ru.forkin.springcourse.cloudstorage.exception.person;

public class PersonNotFoundException extends RuntimeException{
    public PersonNotFoundException(String message) {
        super(message);
    }
}
