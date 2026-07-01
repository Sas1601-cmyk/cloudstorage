package ru.forkin.springcourse.cloudstorage.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.forkin.springcourse.cloudstorage.dto.storage.OutputDirectoryDto;
import ru.forkin.springcourse.cloudstorage.dto.storage.OutputFileDto;
import ru.forkin.springcourse.cloudstorage.dto.storage.OutputStorageDto;
import ru.forkin.springcourse.cloudstorage.exception.storage.DuplicatedResourceException;
import ru.forkin.springcourse.cloudstorage.exception.storage.InvalidPathException;
import ru.forkin.springcourse.cloudstorage.exception.storage.ResourceNotFoundException;
import ru.forkin.springcourse.cloudstorage.exception.storage.StorageException;
import ru.forkin.springcourse.cloudstorage.util.SearchPathValidator;
import ru.forkin.springcourse.cloudstorage.util.TargetDirectoryValidator;
import ru.forkin.springcourse.cloudstorage.util.UploadPathValidator;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {
    @Value("${app.storage.bucket-name}")
    private String bucketName;
    private final MinioClient minioClient;
    private final PersonService personService;
    private final SearchPathValidator searchPathValidator;
    private final TargetDirectoryValidator targetDirectoryValidator;
    private final UploadPathValidator uploadPathValidator;

    //GET /resource?path=$path
    public OutputStorageDto getResourceByPath(String path, String username) {
        uploadPathValidator.validate(path, username);
        String prefix = getPrefix(username);
        String fullPath = prefix.concat(path);
        if (fullPath.endsWith("/")){
            if(isExistsDirectory(fullPath)){
                return new OutputDirectoryDto(getDirectoryPath(path), getDirectoryName(path));
            }else{ throw new ResourceNotFoundException("Directory not found");}}
        else{ try {StatObjectResponse stat = minioClient.statObject(StatObjectArgs
                        .builder().bucket(bucketName).object(fullPath).build());
                return new OutputFileDto(getFilePath(path), getFileName(path), stat.size());
            } catch (ErrorResponseException e) {
                if(e.errorResponse().code().equals("NoSuchKey")){throw new ResourceNotFoundException("File not found"); }
                else{throw new StorageException("Storage error");}
            } catch (Exception e){throw new StorageException(e.getMessage());}}
    }

    //DELETE /resource?path=$path
    public void deleteResourceByPath(String path, String username) {
        uploadPathValidator.validate(path, username);
        String prefix = getPrefix(username);
        String fullPath = prefix.concat(path);
        if (fullPath.endsWith("/")) {
            try { boolean exists = isExistsDirectory(fullPath);
                if (!exists) {throw new ResourceNotFoundException("Directory not found: " + fullPath);}
                Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(bucketName).prefix(fullPath).recursive(true).build());
                List<DeleteObject> objectsToDelete = new ArrayList<>();
                for (Result<Item> result : results) {
                    objectsToDelete.add(new DeleteObject(result.get().objectName()));}
                Iterable<Result<DeleteError>> errors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                                .bucket(bucketName).objects(objectsToDelete).build());
                for (Result<DeleteError> errorResult : errors) {
                    DeleteError error = errorResult.get();} //log
            } catch (ResourceNotFoundException e) {throw e;}
            catch (Exception e) {
                throw new StorageException("Storage error for delete directory");}
        } else { try { minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName).object(fullPath).build());
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName).object(fullPath).build());
            } catch (ErrorResponseException e) {
                if ("NoSuchKey".equals(e.errorResponse().code())) {
                    throw new ResourceNotFoundException("File not found: " + fullPath);
                } else {throw new StorageException("Storage error for delete file: " + e.errorResponse().message());}
            } catch (Exception e) {throw new StorageException(e.getMessage());}}
    }

    // GET /resource/download?path=$path
    public InputStreamResource downloadResourceByPath(String path, String username) {
        uploadPathValidator.validate(path, username);
        String prefix = getPrefix(username);
        String fullPath = prefix.concat(path);
        if (fullPath.endsWith("/")) {
            try {boolean exists = isExistsDirectory(fullPath);
                if (!exists) {throw new ResourceNotFoundException("Directory not found");}
                return new InputStreamResource(convertToZip(fullPath));
            } catch (ResourceNotFoundException e) {throw e;}
            catch (Exception e) {throw new StorageException("Storage error for download directory to Zip");}
        } else { try { minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName).object(fullPath).build());
                InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucketName).object(fullPath).build());
                return new InputStreamResource(inputStream);
            } catch (ErrorResponseException e) {
                if ("NoSuchKey".equals(e.errorResponse().code())) {
                    throw new ResourceNotFoundException("File not found");
                } else {throw new StorageException("Storage error for download file");}
            } catch (Exception e) {
                throw new StorageException("Storage error during file download");}}
    }

    private InputStream convertToZip(String fullPath) {
        try { PipedOutputStream out = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(out);
            new Thread(() -> {
                try (ZipOutputStream zos = new ZipOutputStream(out)) {
                    minioClient.listObjects(ListObjectsArgs.builder()
                                    .bucket(bucketName).prefix(fullPath).recursive(true).build())
                            .forEach(result -> {
                                try { Item item = result.get();
                                    String name = item.objectName().substring(fullPath.length());
                                    if (item.isDir()) {
                                        zos.putNextEntry(new ZipEntry(name));
                                        zos.closeEntry();
                                    } else {
                                        zos.putNextEntry(new ZipEntry(name));
                                        minioClient.getObject(GetObjectArgs.builder().bucket(bucketName)
                                                .object(item.objectName()).build()).transferTo(zos);
                                        zos.closeEntry();
                                    }} catch (Exception e) {
                                    throw new StorageException("Failed to create zip for directory");
                                }});} catch (Exception e) {System.err.println("Zip error: " + e.getMessage());}
                finally { try { out.close(); } catch (IOException ignored) {}}}).start();
            return in;
        } catch (IOException e) {
            throw new StorageException("Failed to create zip for directory");}}

    // GET /resource/move?from=$from&to=$to
    public OutputStorageDto moveResource(String pathFrom, String pathTo, String username) {
        uploadPathValidator.validate(pathFrom, username); targetDirectoryValidator.validate(pathTo, username);
        String prefix = getPrefix(username);
        String normalizedTo;
        boolean isSourceDir = pathFrom.endsWith("/");
        if (pathTo.isEmpty()) { String sourceName = pathFrom.endsWith("/")
                    ? pathFrom.substring(0, pathFrom.length() - 1).substring(pathFrom.lastIndexOf('/') + 1) + "/"
                    : pathFrom.substring(pathFrom.lastIndexOf('/') + 1);
            normalizedTo = sourceName;
        } else { if (isSourceDir) {normalizedTo = pathTo.endsWith("/") ? pathTo : pathTo + "/";}
            else {normalizedTo = pathTo.endsWith("/") ? pathTo.substring(0, pathTo.length() - 1) : pathTo;}}
        String fullFrom = prefix.concat(pathFrom);
        String fullTo = prefix.concat(normalizedTo);
        if (isSourceDir) { boolean sourceExists = false;
            try { sourceExists = isExistsDirectory(fullFrom);
            } catch (Exception e) { throw new StorageException("Error checking source directory"); }
            if (!sourceExists) throw new ResourceNotFoundException("Source directory not found");
            if (pathFrom.equals(normalizedTo)) {
                return new OutputDirectoryDto(getDirectoryPath(pathFrom), getDirectoryName(pathFrom) + "/");}
            boolean destTaken = false;
            try { destTaken = isExistsDirectory(pathTo);
            } catch (Exception e) { throw new StorageException("Error checking destination"); }
            if (destTaken) throw new DuplicatedResourceException("Target resource already exists");
            if (!normalizedTo.isEmpty() && !normalizedTo.equals(getDirectoryName(normalizedTo) + "/")) {
                String parentTo = getDirectoryPath(normalizedTo);
                String fullParentTo = prefix.concat(parentTo);
                boolean parentExists = false;
                try {parentExists = isExistsDirectory(fullParentTo);
                } catch (Exception e) { throw new StorageException("Error checking target parent"); }
                if (!parentExists) throw new ResourceNotFoundException("Target parent directory not found");}
            try { moveDirectoryRecursively(fullFrom, fullTo); }
            catch (StorageException e) { throw e; }
            catch (Exception e) { throw new StorageException("Failed to move directory"); }
            return new OutputDirectoryDto(
                    getDirectoryPath(normalizedTo),
                    getDirectoryName(normalizedTo) + "/");
        }  else { StatObjectResponse statFrom = null;
            try { statFrom = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName).object(fullFrom).build());
            } catch (ErrorResponseException e) {
                if ("NoSuchKey".equals(e.errorResponse().code()))
                    throw new ResourceNotFoundException("Source file not found");
                throw new StorageException("Error checking source file");
            } catch (Exception e) { throw new StorageException("Storage error during move check"); }
            boolean destTaken = false;
            try { minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName).object(fullTo).build());
                destTaken = true;
            } catch (ErrorResponseException e) {
                if ("NoSuchKey".equals(e.errorResponse().code())) destTaken = false;
                else throw new StorageException("Error checking destination file");
            } catch (Exception e) { throw new StorageException("Storage error during dest check"); }
            if (destTaken) throw new DuplicatedResourceException("Target file already exists");
            String parentTo = getFilePath(normalizedTo);
            if (!parentTo.isEmpty()) {
                String fullParentTo = prefix.concat(parentTo);
                boolean parentExists = false;
                try { parentExists = isExistsDirectory(fullParentTo);
                } catch (Exception e) { throw new StorageException("Error checking target parent"); }
                if (!parentExists) throw new ResourceNotFoundException("Target parent directory not found");}
            try { moveFileToNewPlace(fullFrom, fullTo); }
            catch (Exception e) { throw new StorageException("Failed to move file"); }
            return new OutputFileDto(getFilePath(normalizedTo), getFileName(normalizedTo), statFrom.size());}
    }

    private void moveDirectoryRecursively(String sourcePrefix, String destPrefix) {
        try { Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName).prefix(sourcePrefix).recursive(true).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                String oldKey = item.objectName();
                String newKey = destPrefix + oldKey.substring(sourcePrefix.length());
                minioClient.copyObject(CopyObjectArgs.builder().bucket(bucketName).object(newKey)
                        .source(CopySource.builder().bucket(bucketName).object(oldKey).build()).build());
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(oldKey).build());}
        } catch (Exception e) { throw new StorageException("Failed to move directory");}}

    private void moveFileToNewPlace(String sourceObject, String destObject) {
        try { minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(bucketName).object(destObject).source(CopySource.builder()
                        .bucket(bucketName).object(sourceObject).build()).build());
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName).object(sourceObject).build());
        } catch (Exception e) { throw new StorageException("Storage error for rename file");}}


    // GET /resource/search?query=$query
    public List<OutputStorageDto> searchResourceByQuery(String queryParam, String username) {
        searchPathValidator.validate(queryParam, username);
        if (queryParam.isBlank()){ log.error("Path fir search is Blank; person={}", username);
            throw new InvalidPathException("Path fir search is Blank");}
        String prefix = getPrefix(username);
        List<OutputStorageDto> dtoList = new ArrayList<>();
        Set<String> addedPaths = new HashSet<>();
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName).prefix(prefix).recursive(true).build());
        try { for (Result<Item> result : results) {
                Item item = result.get();
                String objName = item.objectName();
                if (objName.equals(prefix)) continue;
                String relativePath = objName.substring(prefix.length());
                if (!relativePath.toLowerCase().contains(queryParam.toLowerCase())) {continue;}
                boolean isDirectory = item.isDir() || relativePath.endsWith("/");
                if (!isDirectory) {
                    String parentPath = getFilePath(relativePath);
                    String fileName = getFileName(relativePath);
                    OutputFileDto fileDto = new OutputFileDto(parentPath, fileName, item.size());
                    if (!dtoList.contains(fileDto)) {
                        dtoList.add(fileDto);}
                } else { String cleanPath = relativePath.endsWith("/")
                            ? relativePath.substring(0, relativePath.length() - 1)
                            : relativePath;
                    int lastSlash = cleanPath.lastIndexOf('/');
                    String folderName = (lastSlash >= 0) ? cleanPath.substring(lastSlash + 1) : cleanPath;
                    String parentPath = (lastSlash >= 0) ? cleanPath.substring(0, lastSlash + 1) : "";
                    if (!addedPaths.contains(relativePath)) {
                        dtoList.add(new OutputDirectoryDto(parentPath, folderName + "/"));
                        addedPaths.add(relativePath);}}
            } if (dtoList.isEmpty()) {throw new InvalidPathException("No resources found by query");}
        } catch (InvalidPathException e) {throw e;}
        catch (Exception e) {throw new StorageException("Search failed: " + e.getMessage());}
        return dtoList;
    }

    // POST resource?path=$path
    public List<OutputStorageDto> uploadFilesByPath(String path, List<MultipartFile> files, String username) {
        searchPathValidator.validate(path, username);
        if(!path.isEmpty() && !path.endsWith("/")){ log.error("Invalid path to target directory; person={}", username);
            throw new InvalidPathException("Invalid path to target directory");}
        if(files.isEmpty()){ log.error("Invalid input files; person={}", username);
            throw new InvalidPathException("Invalid input files");}
        String prefix = getPrefix(username);
        String fullBasePath = prefix.concat(path);
        List<OutputStorageDto> dtoList = new ArrayList<>();
        Set<String> createdDirs = new HashSet<>();
        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) continue;
            String fullObjectKey = fullBasePath.concat(originalName);
            try {minioClient.statObject(StatObjectArgs.builder()
                        .bucket(bucketName).object(fullObjectKey).build());
                throw new DuplicatedResourceException("Resource already exists: " + originalName);
            } catch (DuplicatedResourceException e) {throw e;} catch (ErrorResponseException e) {
                if (!"NoSuchKey".equals(e.errorResponse().code())) {throw new StorageException("Storage check error");}
            } catch (Exception e) {throw new StorageException("MinIO connection error during check");}
            int lastSlash = originalName.lastIndexOf('/');
            if (lastSlash > 0) {
                String dirPath = originalName.substring(0, lastSlash + 1);
                String[] parts = dirPath.split("/");
                StringBuilder currentDir = new StringBuilder();
                for (String part : parts) {
                    if (part.isEmpty()) continue;
                    if (currentDir.length() > 0) currentDir.append("/");
                    currentDir.append(part);
                    String relativeDirPath = currentDir.toString() + "/";
                    String fullDirKey = fullBasePath.concat(relativeDirPath);
                    if (!createdDirs.contains(fullDirKey)) {
                        boolean dirExists = false;
                        try { minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(fullDirKey).build());
                            dirExists = true;}
                        catch (ErrorResponseException e) {
                            if ("NoSuchKey".equals(e.errorResponse().code())) dirExists = false;
                            else throw new StorageException("Dir check error");
                        } catch (Exception e) {throw new StorageException("MinIO error checking directory");}
                        if (!dirExists) {
                            try { minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fullDirKey)
                                        .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                                        .contentType("application/x-directory").build());
                            } catch (Exception e) {throw new StorageException("Failed to create directory structure");}}
                        String parentDirPath = path.isEmpty()
                                ? getDirectoryPath(relativeDirPath)
                                : path.concat(getDirectoryPath(relativeDirPath));
                        String dirName = getDirectoryName(relativeDirPath);
                        if (!dirName.endsWith("/")) dirName += "/";
                        dtoList.add(new OutputDirectoryDto(parentDirPath, dirName));
                        createdDirs.add(fullDirKey);}}}
            try { minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fullObjectKey)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType()).build());
                String finalRelativePath = path.concat(originalName);
                String parentPath = getFilePath(finalRelativePath);
                String fileName = getFileName(finalRelativePath);
                dtoList.add(new OutputFileDto(parentPath, fileName, file.getSize()));
            } catch (IOException e) {throw new StorageException("Failed to read file stream");
            } catch (Exception e) {throw new StorageException("Upload failed: " + originalName);}}
        return dtoList;
    }

    //GET /directory?path=$path
    public Set<OutputStorageDto> getDirectoryObjectsByPath(String path, String username) {
        searchPathValidator.validate(path, username);
        String prefix = getPrefix(username);
        String fullPath = prefix.concat(path);
        Set<OutputStorageDto> dtoList = new HashSet<>();
        if(isExistsDirectory(fullPath)){
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName).prefix(fullPath).recursive(false).build());
            try{ for(Result<Item> result : results){
                    Item item = result.get();
                    String relativeName = item.objectName().substring(fullPath.length());
                    boolean isDirectory = item.isDir() || relativeName.endsWith("/");
                    if(!isDirectory){ dtoList.add(new OutputFileDto(path, relativeName, item.size()));
                    } else { String dirName = relativeName.endsWith("/") ? relativeName : relativeName + "/";
                        dtoList.add(new OutputDirectoryDto(path, dirName));}}
            } catch (Exception e){throw new StorageException("Storage error");}
        } else {throw new ResourceNotFoundException("Target directory not found");}
        return dtoList;
    }

    // POST /directory?path=$path
    public OutputDirectoryDto createEmptyDirectory(String path, String username) {
        searchPathValidator.validate(path, username);
        if(path.isEmpty() || !path.endsWith("/")){ log.error("Invalid path; person={}", username);
            throw new InvalidPathException("Invalid path");}
        String prefix = getPrefix(username);
        String normalizedPath = path.endsWith("/") ? path : path + "/";
        String pathToTargetDir = prefix.concat(normalizedPath);
        String pathToParentDir = getDirectoryPath(pathToTargetDir);
        if(!isExistsDirectory(pathToTargetDir)){
            if (pathToParentDir.isEmpty() || isExistsDirectory(pathToParentDir)){
                try{ minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(pathToTargetDir)
                                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                                    .contentType("application/x-directory").build());
                } catch (Exception e){throw new StorageException("Storage error");}
                String parentPath = getDirectoryPath(normalizedPath);
                String dirName = getDirectoryName(normalizedPath);
                if (!dirName.endsWith("/")) dirName += "/";
                return new OutputDirectoryDto(parentPath, dirName);
            } else { throw new ResourceNotFoundException("Parent directory not exists");}
        } else {throw new DuplicatedResourceException("Target directory already exists");}
    }

    public String getDownloadFileName(String path) {
        if (path.endsWith("/")) {
            String normalized = path.substring(0, path.length() - 1);
            int lastSlash = normalized.lastIndexOf('/');
            String folderName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
            return folderName + ".zip";}
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;}


    private String getPrefix(String username){
        Integer userId = personService.getPersonIdByUsername(username);
        return "user-".concat(userId.toString()).concat("-files/");}

    private String getFilePath(String path) {
        int last = path.lastIndexOf('/');
        return last == -1 ? "" : path.substring(0, last + 1);}

    private String getFileName(String path) {
        int last = path.lastIndexOf('/');
        return last == -1 ? path : path.substring(last + 1);}

    private String getDirectoryPath(String path) {
        int last = path.lastIndexOf('/', path.length() - 2);
        return path.substring(0, last + 1);}

    private String getDirectoryName(String path) {
        int last = path.lastIndexOf('/', path.length() - 2);
        return path.substring(last + 1);}

    private boolean isExistsDirectory(String fullPath){
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName)
                .prefix(fullPath).maxKeys(1).build()).iterator().hasNext();}

    @PostConstruct
    private void initBucket(){
        try{ boolean exists= minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
            if(!exists){ minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());}
        }catch (Exception e){ log.error("Failed to initialize storage bucket");
            throw new RuntimeException("Failed to initialize storage bucket");}}
}