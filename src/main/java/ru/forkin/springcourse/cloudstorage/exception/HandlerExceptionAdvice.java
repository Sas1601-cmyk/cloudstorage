package ru.forkin.springcourse.cloudstorage.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.forkin.springcourse.cloudstorage.dto.ErrorResponse;
import ru.forkin.springcourse.cloudstorage.exception.person.PersonDuplicatedException;
import ru.forkin.springcourse.cloudstorage.exception.person.PersonNotFoundException;
import ru.forkin.springcourse.cloudstorage.exception.storage.DuplicatedResourceException;
import ru.forkin.springcourse.cloudstorage.exception.storage.InvalidPathException;
import ru.forkin.springcourse.cloudstorage.exception.storage.ResourceNotFoundException;
import ru.forkin.springcourse.cloudstorage.exception.storage.StorageException;

@RestControllerAdvice
public class HandlerExceptionAdvice {

    // Person
    @ExceptionHandler(PersonDuplicatedException.class)
    public ResponseEntity<?> handlePersonDuplicatedExc(PersonDuplicatedException e){
        return ResponseEntity
                .status(409)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlePersonValidationExc(MethodArgumentNotValidException e){
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity
                .status(401)
                .body(new ErrorResponse("Bad credentials"));
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<?> handlePersonNotFoundExc(PersonNotFoundException e){
        return ResponseEntity
                .status(404)
                .body(new ErrorResponse(e.getMessage()));
    }

    //Storage
    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<?> handleInvalidPathExc(InvalidPathException e){
        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundExc(ResourceNotFoundException e){
        return ResponseEntity
                .status(404)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<?> handleStorageExc(StorageException e){
        return ResponseEntity
                .status(500)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DuplicatedResourceException.class)
    public ResponseEntity<?> handleDuplicatedResourceExc(DuplicatedResourceException e){
        return ResponseEntity
                .status(409)
                .body(new ErrorResponse(e.getMessage()));
    }

    //Server error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknownExc(Exception e){
        return ResponseEntity
                .status(500)
                .body(new ErrorResponse(e.getMessage()));
    }
}