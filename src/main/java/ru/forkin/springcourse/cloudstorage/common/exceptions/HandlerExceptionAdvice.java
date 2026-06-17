package ru.forkin.springcourse.cloudstorage.common.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.forkin.springcourse.cloudstorage.person.exceptions.PersonDuplicatedException;
import ru.forkin.springcourse.cloudstorage.person.exceptions.PersonNotFoundException;
import ru.forkin.springcourse.cloudstorage.person.exceptions.PersonValidationException;

@RestControllerAdvice
public class HandlerExceptionAdvice {

    @ExceptionHandler(PersonDuplicatedException.class) // todo +
    public ResponseEntity<?> handlePersonDuplicatedExc(PersonDuplicatedException e){
        return ResponseEntity
                .status(409)
                .body(new PersonErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(PersonValidationException.class) // todo +
    public ResponseEntity<?> handlePersonValidationExc(PersonValidationException e){
        return ResponseEntity
                .status(400)
                .body(new PersonErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity
                .status(401)
                .body(new PersonErrorResponse("Bad credentials"));
    }

    @ExceptionHandler(PersonNotFoundException.class) // todo
    public ResponseEntity<?> handlePersonNotFoundExc(PersonNotFoundException e){
        return ResponseEntity
                .status(404)
                .body(new PersonErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class) // todo +
    public ResponseEntity<?> handleUnknownExc(Exception e){
        return ResponseEntity
                .status(500)
                .body(new PersonErrorResponse(e.getMessage()));
    }

}