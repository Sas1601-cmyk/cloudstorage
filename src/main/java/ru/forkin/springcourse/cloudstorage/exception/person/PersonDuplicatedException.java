package ru.forkin.springcourse.cloudstorage.exception.person;

public class PersonDuplicatedException extends RuntimeException {
    public PersonDuplicatedException(String message) {
        super(message);
    }
}
