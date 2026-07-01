package ru.forkin.springcourse.cloudstorage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.forkin.springcourse.cloudstorage.exception.storage.InvalidPathException;

@Slf4j
@Component
public class UploadPathValidator {
    public void validate(String path, String username) {
        if (path == null || path.isBlank() || path.contains("..") || path.equals(".")) {
            log.error("Invalid path to target directory by person={}; path is blank or contains '..' ", username);
            throw new InvalidPathException("Invalid path");}
        if (path.startsWith("/") || path.contains("//")){
            log.error("Invalid path to target directory by person={}; path starts with '/' or contains '//' ", username);
            throw new InvalidPathException("Invalid path");
        }
    }
}

