package com.chada.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/images")
public class FileUploadController {

    @Value("${file.upload-dir.absolute}")
    private String uploadDir;

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/octet-stream";
                long contentLength = 0;
                long lastModified = 0;
                
                try {
                    String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    switch (fileExtension) {
                        case "jpg":
                        case "jpeg":
                            contentType = "image/jpeg";
                            break;
                        case "png":
                            contentType = "image/png";
                            break;
                        case "gif":
                            contentType = "image/gif";
                            break;
                        case "webp":
                            contentType = "image/webp";
                            break;
                    }
                    
                    // Get file metadata for caching
                    if (Files.exists(filePath)) {
                        contentLength = Files.size(filePath);
                        lastModified = Files.getLastModifiedTime(filePath).toInstant().toEpochMilli();
                    }
                } catch (Exception e) {
                    // Use default values
                }

                // Build response with aggressive caching for images
                ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        // Cache for 1 year (images don't change often)
                        .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                        // ETag for cache validation
                        .eTag(fileName + "-" + lastModified)
                        // Last modified header
                        .lastModified(Instant.ofEpochMilli(lastModified))
                        // Content length
                        .contentLength(contentLength)
                        // Enable compression
                        .header(HttpHeaders.VARY, "Accept-Encoding")
                        // CORS headers for frontend
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, HEAD, OPTIONS")
                        .header(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");

                return responseBuilder.body(resource);
            } else {
                return ResponseEntity.notFound()
                        .cacheControl(CacheControl.noCache())
                        .build();
            }
        } catch (IOException e) {
            return ResponseEntity.notFound()
                    .cacheControl(CacheControl.noCache())
                    .build();
        }
    }
    
    @GetMapping("/{fileName:.+}/thumbnail")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String fileName, 
                                                   @RequestParam(defaultValue = "200") int size) {
        // For now, return the original image
        // In the future, you can add thumbnail generation here
        return getImage(fileName);
    }
}

