package ru.forkin.springcourse.cloudstorage.dto.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OutputDirectoryDto extends OutputStorageDto{
    private String path;
    private String name;
    private final String type = "DIRECTORY";

    public OutputDirectoryDto(String path, String name) {
        this.path = path;
        this.name = name;
    }
}
