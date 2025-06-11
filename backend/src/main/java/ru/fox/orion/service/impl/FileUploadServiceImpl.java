package ru.fox.orion.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.fox.orion.service.FileUploadService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${s3.bucket}")
    private String bucket;

    private final S3Client s3Client;

    @Override
    public void upload(MultipartFile file, String key) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .cacheControl("max-age=315360000")
                .acl(ObjectCannedACL.PRIVATE)
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

    }

    @Override
    public byte[] download(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            var inputStream = s3Client.getObject(getObjectRequest);
            byte[] data = inputStream.readAllBytes();
            inputStream.close();
            return data;
        } catch (S3Exception | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception ignored) {

        }
    }

    @Override
    public void upload(byte[] data, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength((long) data.length)
                .contentType("application/pdf")
                .cacheControl("max-age=315360000")
                .acl(ObjectCannedACL.PRIVATE)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
    }
}