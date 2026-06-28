package ru.forkin.springcourse.cloudstorage.storage;

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
import ru.forkin.springcourse.cloudstorage.storage.dto.OutputDirectoryDto;
import ru.forkin.springcourse.cloudstorage.storage.dto.OutputStorageDto;
import ru.forkin.springcourse.cloudstorage.storage.exceptions.InvalidPathException;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Endpoint for work with minio")
public class FileStorageController {
    private final FileStorageService service;

    // GET /resource?path=$path
    @GetMapping("/resource")
    @Operation(summary = "Get resource by current path")
    public ResponseEntity<?> getResourceByPath(
            @RequestParam String path, @AuthenticationPrincipal UserDetails userDetails){
        log.info("getResourceByPath path={}", path);
        if (!isValidPath(path)){throw new InvalidPathException("Invalid path");}
        OutputStorageDto dto = service.getResourceByPath(path, userDetails.getUsername());
        return ResponseEntity.status(200).body(dto);
    }

    // DELETE /resource?path=$path
    @DeleteMapping("/resource")
    @Operation(summary = "Delete resource by current path")
    public ResponseEntity<?> deleteResourceByPath(
            @RequestParam String path, @AuthenticationPrincipal UserDetails userDetails){
        log.info("deleteResourceByPath path={}", path);
        if (!isValidPath(path)){throw new InvalidPathException("Invalid path");}
        service.deleteResourceByPath(path, userDetails.getUsername());
        return ResponseEntity.status(204).build();
    }

    // GET /resource/download?path=$path
    @GetMapping("/resource/download")
    @Operation(summary = "Download resource by current path")
    public ResponseEntity<?> downloadResourceByPath(
            @RequestParam String path, @AuthenticationPrincipal UserDetails userDetails){
        log.info("downloadResourceByPath path={}", path);
        if(!isValidPath(path)){ throw new InvalidPathException("Invalid path");}
        InputStreamResource resource = service.downloadResourceByPath(path, userDetails.getUsername());
        String fileName = service.getDownloadFileName(path);
        return ResponseEntity
                .status(200)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    // GET /resource/move?from=$from&to=$to
    @GetMapping("/resource/move")
    @Operation(summary = "Move resource by pathFrom to pathTo")
    public ResponseEntity<?> moveResourceByPathFromToPathTo( @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String from, @RequestParam String to){
        log.info("downloadResourceByPath pathFrom={} pathTo={}", from, to);
        if(!isValidPath(from)){ throw new InvalidPathException("Invalid path from");}
        if(!isValidPathToTargetDirectory(to)){ throw new InvalidPathException("Invalid path to");}
        OutputStorageDto dto = service.moveResourceByPathFromToPathTo(from, to, userDetails.getUsername());
        return ResponseEntity.status(200).body(dto);
    }

    // GET /resource/search?query=$query
    @GetMapping("/resource/search")
    @Operation(summary = "Search resource by query")
    public ResponseEntity<?> searchResourceByQuery(
            @RequestParam String query, @AuthenticationPrincipal UserDetails userDetails){
        log.info("searchResourceByQuery query={}", query);
        if (query.isBlank()){throw new InvalidPathException("Invalid path(isBlank)");}
        if (!isValidPathForSearch(query)){throw new InvalidPathException("Invalid path");}
        List<OutputStorageDto> dtoList = service.searchResourceByQuery(query, userDetails.getUsername());
        return ResponseEntity.status(200).body(dtoList);
    }

    // POST resource?path=$path
    @PostMapping("/resource")
    @Operation(summary = "Upload resource to target path")
    public ResponseEntity<?> uploadResourceByPath(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") String path, @RequestParam("object") List<MultipartFile> files){
        log.info("uploadResourceByPath path={} files={}", path, files);
        if(!isValidPathForSearch(path) || (!path.isEmpty() && !path.endsWith("/"))){
            throw new InvalidPathException("Invalid path to target directory");}
        if(files.isEmpty()){
            throw new InvalidPathException("Invalid input files");}
        List<OutputStorageDto> dtoList = service.uploadFilesByPath(path, files, userDetails.getUsername());
        return ResponseEntity.status(201).body(dtoList);
    }

    //GET /directory?path=$path
    @GetMapping("/directory")
    @Operation(summary = "Get resources by current directory")
    public ResponseEntity<?> getDirectoryObjectsByPath(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") String path){
        log.info("getDirectoryObjectsByPath path={}",path );
        if(!isValidPathForSearch(path)){
            throw new InvalidPathException("Invalid path");}
        Set<OutputStorageDto> dtoList = service.getDirectoryObjectsByPath(path, userDetails.getUsername());
        return ResponseEntity.status(200).body(dtoList);
    }

    // POST /directory?path=$path
    @PostMapping("/directory")
    @Operation(summary = "Create empty directory from current path")
    public ResponseEntity<?> createEmptyDirectory(@AuthenticationPrincipal UserDetails userDetails,
              @RequestParam(required = false, defaultValue = "") String path){
        log.info("createEmptyDirectory path={}",path );
        if(!isValidPathForSearch(path) || path.isEmpty() || !path.endsWith("/")){
            throw new InvalidPathException("Invalid path");}
        OutputDirectoryDto outputDirectoryDto = service.createEmptyDirectory(path, userDetails.getUsername());
        return ResponseEntity.status(201).body(outputDirectoryDto);
    }

    private boolean isValidPathForSearch(String path){
        if(path.startsWith("..") || path.startsWith(".")
                || path.startsWith("/") || path.contains("//")){ return false; }
        return true;}

    private boolean isValidPath(String path) {
        if (path == null || path.isBlank()) return false;
        if (path.contains("..")) return false;
        if (path.startsWith("/") || path.contains("//")) return false;
        if (path.equals(".")) return false;
        return true;}

    private boolean isValidPathToTargetDirectory(String path) {
        if (path == null) return false;
        if (path.startsWith("..") || path.startsWith(".")
                || path.startsWith("/") || path.contains("//")) {
            return false;
        } return true;}
}