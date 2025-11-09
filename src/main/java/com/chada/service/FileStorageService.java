package com.chada.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    
    // Maximum image dimensions (reduce if too large)
    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1920;
    private static final float JPEG_QUALITY = 0.85f; // 85% quality for good balance

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
            fileExtension = originalFileName.substring(lastDotIndex).toLowerCase();
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Optimize and save file
        Path targetLocation = Paths.get(uploadDir).resolve(uniqueFileName);
        
        // Check if it's an image file
        if (isImageFile(fileExtension)) {
            try {
                // Optimize image before saving
                InputStream optimizedImageStream = optimizeImage(file.getInputStream(), fileExtension);
                Files.copy(optimizedImageStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                optimizedImageStream.close();
            } catch (Exception e) {
                // If optimization fails, save original
                System.err.println("Warning: Image optimization failed, saving original: " + e.getMessage());
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            // Not an image, save as-is
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        return uniqueFileName;
    }
    
    private boolean isImageFile(String extension) {
        if (extension == null) return false;
        String ext = extension.toLowerCase();
        return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || 
               ext.equals(".gif") || ext.equals(".webp") || ext.equals(".bmp");
    }
    
    private InputStream optimizeImage(InputStream originalStream, String extension) throws IOException {
        try {
            // Read original image
            BufferedImage originalImage = ImageIO.read(originalStream);
            if (originalImage == null) {
                throw new IOException("Could not read image");
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // Calculate new dimensions (maintain aspect ratio)
            int newWidth = originalWidth;
            int newHeight = originalHeight;
            
            if (originalWidth > MAX_IMAGE_WIDTH || originalHeight > MAX_IMAGE_HEIGHT) {
                double scale = Math.min(
                    (double) MAX_IMAGE_WIDTH / originalWidth,
                    (double) MAX_IMAGE_HEIGHT / originalHeight
                );
                newWidth = (int) (originalWidth * scale);
                newHeight = (int) (originalHeight * scale);
            }
            
            // Resize image if needed
            BufferedImage resizedImage;
            if (newWidth != originalWidth || newHeight != originalHeight) {
                // Use high-quality scaling
                resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g.dispose();
            } else {
                // Convert to RGB if needed (for JPEG)
                if (extension.equals(".jpg") || extension.equals(".jpeg")) {
                    resizedImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, null);
                    g.dispose();
                } else {
                    resizedImage = originalImage;
                }
            }
            
            // Compress and write to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = extension.substring(1); // Remove the dot
            
            if (format.equals("jpg") || format.equals("jpeg")) {
                // Use JPEG with quality setting
                java.util.Iterator<javax.imageio.ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
                if (!writers.hasNext()) {
                    // Fallback to default write if no JPEG writer available
                    ImageIO.write(resizedImage, "jpeg", baos);
                } else {
                    javax.imageio.ImageWriter writer = writers.next();
                    javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                    writer.setOutput(ios);
                    
                    javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                    if (param.canWriteCompressed()) {
                        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                        param.setCompressionQuality(JPEG_QUALITY);
                    }
                    
                    writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), param);
                    writer.dispose();
                    ios.close();
                }
            } else {
                // For PNG and other formats, use default compression
                ImageIO.write(resizedImage, format, baos);
            }
            
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            // If optimization fails, return original stream
            throw new IOException("Image optimization failed: " + e.getMessage(), e);
        }
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

