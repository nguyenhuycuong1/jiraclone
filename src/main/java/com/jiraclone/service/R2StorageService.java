package com.jiraclone.service;

import com.jiraclone.config.R2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2StorageService {

    private final S3Client s3Client;
    private final R2Properties r2Properties;

    public String upload(String objectKey, byte[] data, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) data.length)
                .cacheControl("public, max-age=31536000") // Cache for 1 year
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));

        return r2Properties.getPublicUrl() + "/" + objectKey;
    }

    public void delete(String objectKey) {
        try {
            s3Client.deleteObject(b -> b.bucket(r2Properties.getBucketName()).key(objectKey));
        } catch (S3Exception e) {
            log.warn("Failed to delete object {} from R2: {}", objectKey, e.awsErrorDetails().errorMessage());
        }
    }

    public String extractKey(String fullUrl) {
        return fullUrl.replace(r2Properties.getPublicUrl() + "/", "");
    }
}
