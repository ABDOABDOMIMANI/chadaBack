package com.chada.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir.absolute}")
    private String uploadDir;

    public List<String> storeFiles(MultipartFile[] files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Limit to 4 images
        int maxImages = Math.min(files.length, 4);
        
        for (int i = 0; i < maxImages; i++) {
            MultipartFile file = files[i];
            if (file != null && !file.isEmpty()) {
                String fileName = storeFile(file);
                if (fileName != null) {
                    fileUrls.add("/api/images/" + fileName);
                }
            }
        }

        return fileUrls;
    }

    private String storeFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return null;
        }

        // Generate unique file name
        String fileExtension = "";
        int lastDotIndex = originalFileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = originalFileName.substring(lastDotIndex);
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path targetLocation = Paths.get(uploadDir).resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public boolean deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

