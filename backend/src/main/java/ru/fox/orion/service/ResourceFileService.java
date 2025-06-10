package ru.fox.orion.service;

import org.springframework.web.multipart.MultipartFile;

public interface ResourceFileService {

    byte[] getActualFile();

    void uploadFile(MultipartFile file);

    String getPdfFile();

}