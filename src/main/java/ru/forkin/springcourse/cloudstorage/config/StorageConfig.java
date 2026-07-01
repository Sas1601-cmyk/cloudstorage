package ru.forkin.springcourse.cloudstorage.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    @Value("${app.storage.url}")
    private String url;

    @Value("${app.storage.access-key}")
    private String accessKey;

    @Value("${app.storage.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
            .endpoint(url)
            .credentials(accessKey, secretKey)
            .build();
    }
}
