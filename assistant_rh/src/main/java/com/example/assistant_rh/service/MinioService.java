package com.example.assistant_rh.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation
    .Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart
    .MultipartFile;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-documents}")
    private String bucket;

    // Initialize bucket at startup
    public void initBucket() {
        try {
            boolean exists = minioClient
                .bucketExists(BucketExistsArgs
                    .builder()
                    .bucket(bucket)
                    .build());
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucket)
                        .build());
                log.info("Bucket created : {}",
                    bucket);
            }
        } catch (Exception e) {
            log.error("Bucket init error : {}",
                e.getMessage());
        }
    }

    // ✅ Upload — accepts MultipartFile + employeeId
    public String uploadFile(
            MultipartFile file,
            Long employeeId) throws Exception {

        String originalName =
            file.getOriginalFilename();
        String extension = originalName != null
            && originalName.contains(".")
            ? originalName.substring(
                originalName.lastIndexOf('.'))
            : "";

        // Unique key: employeeId/timestamp_name
        String minioKey = "employee-"
            + employeeId + "/"
            + System.currentTimeMillis()
            + "_" + originalName;

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .object(minioKey)
                .stream(
                    file.getInputStream(),
                    file.getSize(), -1)
                .contentType(
                    file.getContentType())
                .build());

        log.info("File uploaded to MinIO : {}",
            minioKey);
        return minioKey;
    }

    // Generate download URL (1 hour)
    public String generateDownloadUrl(
            String minioKey) throws Exception {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(minioKey)
                .expiry(1, TimeUnit.HOURS)
                .build());
    }

    // delete file
    public void deleteFile(
            String minioKey) throws Exception {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(minioKey)
                .build());
        log.info("File deleted : {}", minioKey);
    }
}