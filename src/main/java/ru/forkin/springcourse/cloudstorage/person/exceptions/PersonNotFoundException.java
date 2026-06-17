package ru.forkin.springcourse.cloudstorage.person.exceptions;

public class PersonNotFoundException extends RuntimeException{
    public PersonNotFoundException(String message) {
        super(message);
    }
}
