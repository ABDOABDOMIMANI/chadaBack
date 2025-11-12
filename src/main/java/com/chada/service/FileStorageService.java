package com.chada.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Value("${file.upload-dir.absolute}")
    private String uploadDir;
    
    @Value("${image.optimization.enabled:true}")
    private boolean optimizationEnabled;
    
    @Value("${image.optimization.skip-size-threshold:2097152}")
    private long skipOptimizationSize;
    
    // Maximum image dimensions (reduced for faster processing)
    private static final int MAX_IMAGE_WIDTH = 1600;
    private static final int MAX_IMAGE_HEIGHT = 1600;
    private static final float JPEG_QUALITY = 0.75f; // 75% quality for faster processing

    public List<String> storeFiles(MultipartFile[] files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Limit to 4 images
        int maxImages = Math.min(files.length, 4);
        
        // Process images in parallel for better performance
        List<java.util.concurrent.Future<String>> futures = new ArrayList<>();
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(Math.min(maxImages, 4));
        
        try {
            for (int i = 0; i < maxImages; i++) {
                MultipartFile file = files[i];
                if (file != null && !file.isEmpty()) {
                    final MultipartFile finalFile = file;
                    java.util.concurrent.Future<String> future = executor.submit(() -> {
                        try {
                            return storeFile(finalFile);
                        } catch (IOException e) {
                            logger.error("Error storing file {}: {}", finalFile.getOriginalFilename(), e.getMessage());
                            return null;
                        }
                    });
                    futures.add(future);
                }
            }
            
            // Collect results
            for (java.util.concurrent.Future<String> future : futures) {
                try {
                    String fileName = future.get();
                    if (fileName != null) {
                        fileUrls.add("/api/images/" + fileName);
                    }
                } catch (Exception e) {
                    logger.error("Error getting file upload result: {}", e.getMessage());
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
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
        if (isImageFile(fileExtension) && optimizationEnabled) {
            // Only optimize very large files (>5MB) to speed up uploads
            long fileSize = file.getSize();
            if (fileSize > 0 && fileSize < skipOptimizationSize) {
                // Small/medium file, save directly without optimization for speed
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Saved image without optimization (size: {} bytes): {}", fileSize, uniqueFileName);
            } else {
                try {
                    // Only optimize very large files
                    InputStream imageStream = file.getInputStream();
                    InputStream optimizedImageStream = optimizeImage(imageStream, fileExtension);
                    
                    // Copy optimized image
                    Files.copy(optimizedImageStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                    optimizedImageStream.close();
                    
                    logger.debug("Successfully optimized and saved large image: {} (original: {} bytes)", uniqueFileName, fileSize);
                } catch (Exception e) {
                    // If optimization fails, save original immediately - don't let optimization break uploads
                    logger.warn("Image optimization failed for {}, saving original: {}", uniqueFileName, e.getMessage());
                    try {
                        // Reset and save original
                        InputStream originalStream = file.getInputStream();
                        Files.copy(originalStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                        originalStream.close();
                        logger.debug("Saved original image (optimization skipped): {}", uniqueFileName);
                    } catch (IOException ioException) {
                        logger.error("CRITICAL: Failed to save image even as original: {}", uniqueFileName, ioException);
                        throw ioException;
                    }
                }
            }
        } else {
            // Not an image or optimization disabled, save as-is (FASTEST PATH)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Saved file without optimization: {}", uniqueFileName);
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
            
            // Resize image if needed - use fastest scaling algorithm
            BufferedImage resizedImage;
            if (newWidth != originalWidth || newHeight != originalHeight) {
                // Use fastest scaling algorithm for speed
                java.awt.Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_FAST);
                resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                // Use fastest rendering hints
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                g.drawImage(scaledImage, 0, 0, null);
                g.dispose();
            } else {
                // Convert to RGB if needed (for JPEG) - minimal processing
                if (extension.equals(".jpg") || extension.equals(".jpeg")) {
                    resizedImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
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

