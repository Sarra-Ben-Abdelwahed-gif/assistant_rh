package com.example.assistant_rh.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-documents}")
    private String bucket;

    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucket).build());
                log.info("Bucket created : {}", bucket);
            }
        } catch (Exception e) {
            log.error("Error bucket : {}", e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file)
            throws Exception {
        initBucket();
        String key = UUID.randomUUID()
            + "_" + file.getOriginalFilename();
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .stream(file.getInputStream(),
                    file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        log.info("File uploaded : {}", key);
        return key;
    }

    public String generateDownloadUrl(String key)
            throws Exception {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(key)
                .method(Method.GET)
                .expiry(1, TimeUnit.HOURS)
                .build());
    }

    public void deleteFile(String key) throws Exception {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build());
        log.info("File deleted : {}", key);
    }
}