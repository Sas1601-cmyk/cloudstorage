package ru.forkin.springcourse.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.forkin.springcourse.cloudstorage.dto.storage.OutputDirectoryDto;
import ru.forkin.springcourse.cloudstorage.dto.storage.OutputStorageDto;
import ru.forkin.springcourse.cloudstorage.service.StorageServiceImpl;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Endpoint for work with minio")
public class StorageController {
    private final StorageServiceImpl service;

    @GetMapping("/resource")
    @Operation(summary = "Get resource by current path")
    public ResponseEntity<?> getResourceByPath(
            @RequestParam String path, @AuthenticationPrincipal UserDetails userDetails){
        log.info("getResourceByPath path={}", path);
        OutputStorageDto dto = service.getResourceByPath(path, userDetails.getUsername());
        return ResponseEntity.status(200).body(dto);
    }

    @DeleteMapping("/resource")
    @Operation(summary = "Delete resource by current path")
    public ResponseEntity<?> deleteResourceByPath(
            @RequestParam String path, @AuthenticationPrincipal UserDetails userDetails){
        log.info("deleteResourceByPath path={}", path);
        service.deleteResourceByPath(path, userDetails.getUsername());
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/resource/download")
    @Operation(summary = "Download resource by current path")
    public ResponseEntity<?> downloadResourceByPath(
            @RequestParam String path, @AuthenticationPrincipal UserDetails userDetails){
        log.info("downloadResourceByPath path={}", path);
        InputStreamResource resource = service.downloadResourceByPath(path, userDetails.getUsername());
        String fileName = service.getDownloadFileName(path);
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(resource);
    }

    @GetMapping("/resource/move")
    @Operation(summary = "Move resource by pathFrom to pathTo")
    public ResponseEntity<?> moveResource(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String from, @RequestParam String to){
        log.info("moveResource pathFrom={} pathTo={}", from, to);
        OutputStorageDto dto = service.moveResource(from, to, userDetails.getUsername());
        return ResponseEntity.status(200).body(dto);
    }

    @GetMapping("/resource/search")
    @Operation(summary = "Search resource by query")
    public ResponseEntity<?> searchResourceByQuery(
            @RequestParam String query, @AuthenticationPrincipal UserDetails userDetails){
        log.info("searchResourceByQuery query={}", query);
        List<OutputStorageDto> dtoList = service.searchResourceByQuery(query, userDetails.getUsername());
        return ResponseEntity.status(200).body(dtoList);
    }

    @PostMapping("/resource")
    @Operation(summary = "Upload resource to target path")
    public ResponseEntity<?> uploadResourceByPath(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") String path, @RequestParam("object") List<MultipartFile> files){
        log.info("uploadResourceByPath path={} files={}", path, files);
        List<OutputStorageDto> dtoList = service.uploadFilesByPath(path, files, userDetails.getUsername());
        return ResponseEntity.status(201).body(dtoList);
    }

    @GetMapping("/directory")
    @Operation(summary = "Get resources by current directory")
    public ResponseEntity<?> getDirectoryObjectsByPath(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") String path){
        log.info("getDirectoryObjectsByPath path={}", path);
        Set<OutputStorageDto> dtoList = service.getDirectoryObjectsByPath(path, userDetails.getUsername());
        return ResponseEntity.status(200).body(dtoList);
    }

    // POST /directory?path=$path
    @PostMapping("/directory")
    @Operation(summary = "Create empty directory from current path")
    public ResponseEntity<?> createEmptyDirectory(@AuthenticationPrincipal UserDetails userDetails,
              @RequestParam(required = false, defaultValue = "") String path){
        log.info("createEmptyDirectory path={}", path);
        OutputDirectoryDto outputDirectoryDto = service.createEmptyDirectory(path, userDetails.getUsername());
        return ResponseEntity.status(201).body(outputDirectoryDto);
    }
}