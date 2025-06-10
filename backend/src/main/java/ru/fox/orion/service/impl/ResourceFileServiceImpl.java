package ru.fox.orion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.fox.orion.service.FileUploadService;
import ru.fox.orion.service.ResourceFileService;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceFileServiceImpl implements ResourceFileService {

    @Value("${pdf.s3key}")
    private String filenameKey;

    private final FileUploadService fileUploadService;

    private final CacheManager cacheManager;

    private final ApplicationContext applicationContext;

    @Override
    @Cacheable(value = "pdf-file", unless = "#result == null")
    public byte[] getActualFile() {
        return fileUploadService.download(filenameKey);
    }

    @Override
    @CacheEvict(value = "pdf-file", allEntries = true)
    public void uploadFile(MultipartFile file) {
        fileUploadService.upload(file, filenameKey);

    }

    @Override
    public String getPdfFile() {
        ResourceFileService self = applicationContext.getBean(ResourceFileService.class);
        byte[] data = self.getActualFile();
        return new String(data, StandardCharsets.UTF_8);
    }

}