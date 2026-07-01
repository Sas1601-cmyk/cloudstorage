package ru.forkin.springcourse.cloudstorage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.forkin.springcourse.cloudstorage.exception.storage.InvalidPathException;

@Slf4j
@Component
public class SearchPathValidator {
    public void validate(String path, String username){
        if(path.startsWith("..") || path.startsWith(".")
            || path.startsWith("/") || path.contains("//")){
            log.error("Invalid path for search by person={}", username);
            throw new InvalidPathException("Invalid path");
        }
    }
}
