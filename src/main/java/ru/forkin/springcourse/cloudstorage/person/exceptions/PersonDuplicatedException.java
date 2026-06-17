package ru.forkin.springcourse.cloudstorage.person.exceptions;

public class PersonDuplicatedException extends RuntimeException {
    public PersonDuplicatedException(String message) {
        super(message);
    }
}
