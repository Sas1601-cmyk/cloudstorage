package ru.forkin.springcourse.cloudstorage.dto.storage;

import lombok.*;

@Getter
@Setter
@ToString
public class OutputFileDto extends OutputStorageDto{
    private String path;
    private String name;
    private Long size;
    private final String type = "FILE";

    public OutputFileDto(String path, String name, Long size) {
        this.path = path;
        this.name = name;
        this.size = size;
    }
}
