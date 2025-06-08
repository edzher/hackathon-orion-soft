package ru.fox.orion.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    void upload(MultipartFile image, String key);

    void upload(byte[] data, String key);

    byte[] download(String key);

    void delete(String key);

}