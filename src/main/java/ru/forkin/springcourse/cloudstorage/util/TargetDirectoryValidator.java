package ru.forkin.springcourse.cloudstorage.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.forkin.springcourse.cloudstorage.exception.storage.InvalidPathException;

@Slf4j
@Component
public class TargetDirectoryValidator {
    public void validate(String path, String username) {
        if (path == null) {
            log.error("Invalid path to target directory by person={}; path is null", username);
            throw new InvalidPathException("Invalid path - path is null");}
        if (path.startsWith("..") || path.startsWith(".")
                || path.startsWith("/") || path.contains("//")) {
            log.error("Invalid path to target directory by person={}", username);
            throw new InvalidPathException("Invalid path");
        }
    }
}
