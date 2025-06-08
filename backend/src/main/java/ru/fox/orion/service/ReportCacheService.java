package ru.fox.orion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.fox.orion.service.FileUploadService;

@Service
@RequiredArgsConstructor
public class ReportCacheService {

    private final StringRedisTemplate redisTemplate;

    private final FileUploadService fileUploadService;

    public String getS3KeyByHash(String hash) {
        return redisTemplate.opsForValue().get(hash);
    }

    public void saveToCache(String hash, String s3Key) {
        redisTemplate.opsForValue().set(hash, s3Key);
    }

    public byte[] getPdfFromS3(String s3Key) {
        return fileUploadService.download(s3Key);
    }

    public void savePdfToS3(String s3Key, byte[] pdf) {
        fileUploadService.upload(pdf, s3Key);
    }
} 