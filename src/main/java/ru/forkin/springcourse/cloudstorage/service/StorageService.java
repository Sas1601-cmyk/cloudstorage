package ru.forkin.springcourse.cloudstorage.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;
import ru.forkin.springcourse.cloudstorage.dto.storage.OutputDirectoryDto;
import ru.forkin.springcourse.cloudstorage.dto.storage.OutputStorageDto;

import java.util.List;
import java.util.Set;

public interface StorageService {
    OutputStorageDto getResourceByPath(String path, String username);

    void deleteResourceByPath(String path, String username);

    InputStreamResource downloadResourceByPath(String path, String username);

    OutputStorageDto moveResource(String pathFrom, String pathTo, String username);

    List<OutputStorageDto> searchResourceByQuery(String queryParam, String username);

    List<OutputStorageDto> uploadFilesByPath(String path, List<MultipartFile> files, String username);

    Set<OutputStorageDto> getDirectoryObjectsByPath(String path, String username);

    OutputDirectoryDto createEmptyDirectory(String path, String username);
}
