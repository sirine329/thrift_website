package com.thriftby.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDirPath;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = Paths.get(uploadDirPath).toAbsolutePath();
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload: " + uploadDir, e);
        }
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Fichier invalide");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String filename = UUID.randomUUID() + extension;
        try {
            Path destination = uploadDir.resolve(filename);
            file.transferTo(destination);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Impossible de sauvegarder l'image", e);
        }
    }

    public List<String> saveAll(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    urls.add(save(file));
                }
            }
        }
        return urls;
    }
}
