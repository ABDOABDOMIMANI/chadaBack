package com.chada.service;

import com.chada.dto.ProductImageDTO;
import com.chada.entity.Category;
import com.chada.entity.Product;
import com.chada.repository.CategoryRepository;
import com.chada.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Get only active products (with at least one image having quantity > 0)
    public List<Product> getActiveProducts() {
        List<Product> allActive = productRepository.findByActiveTrue();
        // Filter products that have at least one image with quantity > 0
        return allActive.stream()
                .filter(product -> hasAvailableStock(product))
                .collect(Collectors.toList());
    }
    
    // Check if product has at least one image with quantity > 0
    private boolean hasAvailableStock(Product product) {
        if (product.getImageDetails() == null || product.getImageDetails().isEmpty()) {
            return false; // No images, product not available
        }
        
        try {
            List<ProductImageDTO> imageDetails = objectMapper.readValue(
                    product.getImageDetails(),
                    new TypeReference<List<ProductImageDTO>>() {}
            );
            
            // Check if at least one image has quantity > 0
            return imageDetails.stream()
                    .anyMatch(img -> img.getQuantity() != null && img.getQuantity() > 0);
        } catch (Exception e) {
            logger.warn("Failed to parse imageDetails for product {}: {}", product.getId(), e.getMessage());
            return false; // If parsing fails, consider product unavailable
        }
    }

    // ✅ Get all products (admin)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with ID " + id + " not found"));
    }
    
    // ✅ Get products on promotion
    public List<Product> getActivePromotions() {
        return productRepository.findActivePromotions(LocalDate.now());
    }

    // ✅ Create product (frontend sends category.id)
    public Product createProduct(Product product) {
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new RuntimeException("Category ID is required");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        product.setCategory(category);
        product.setActive(true);
        
        // Handle promotion pricing (applies to all images in imageDetails)
        handlePromotionPricing(product);

        Product saved = productRepository.save(product);
        return saved;
    }
    
    // ✅ Update product
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with ID " + id + " not found"));

        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setImageUrl(updatedProduct.getImageUrl());
        existing.setImageUrls(updatedProduct.getImageUrls());
        existing.setImageDetails(updatedProduct.getImageDetails()); // Contains price and quantity per image
        existing.setFragrance(updatedProduct.getFragrance());
        existing.setVolume(updatedProduct.getVolume());
        existing.setActive(updatedProduct.getActive());

        if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getId() != null) {
            Category category = categoryRepository.findById(updatedProduct.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existing.setCategory(category);
        }
        
        // Handle promotion pricing (applies discount to all images in imageDetails)
        if (updatedProduct.getDiscountPercentage() != null && updatedProduct.getDiscountPercentage() > 0) {
            // There's a promotion
            existing.setDiscountPercentage(updatedProduct.getDiscountPercentage());
            existing.setPromotionStartDate(updatedProduct.getPromotionStartDate());
            existing.setPromotionEndDate(updatedProduct.getPromotionEndDate());
            
            // Original price should be provided from frontend (average or base price)
            if (updatedProduct.getOriginalPrice() != null) {
                existing.setOriginalPrice(updatedProduct.getOriginalPrice());
            }
        } else {
            // No promotion - clear promotion fields
            existing.setDiscountPercentage(null);
            existing.setPromotionStartDate(null);
            existing.setPromotionEndDate(null);
            existing.setOriginalPrice(null);
        }

        Product saved = productRepository.save(existing);
        
        // Check if any image has low stock (less than 3) and send alert
        checkLowStockAndSendAlert(saved);
        
        return saved;
    }
    
    private void handlePromotionPricing(Product product) {
        // Promotion pricing is now handled at the image level in imageDetails
        // This method is kept for backward compatibility but doesn't modify product price/stock
        if (product.getDiscountPercentage() == null || product.getDiscountPercentage() <= 0) {
            // No promotion, clear promotion fields
            product.setDiscountPercentage(null);
            product.setOriginalPrice(null);
            product.setPromotionStartDate(null);
            product.setPromotionEndDate(null);
        }
    }
    
    // Check if any image has low stock and send alert
    private void checkLowStockAndSendAlert(Product product) {
        if (!product.getActive() || product.getImageDetails() == null || product.getImageDetails().isEmpty()) {
            return;
        }
        
        try {
            List<ProductImageDTO> imageDetails = objectMapper.readValue(
                    product.getImageDetails(),
                    new TypeReference<List<ProductImageDTO>>() {}
            );
            
            // Check if any image has stock less than 3
            boolean hasLowStock = imageDetails.stream()
                    .anyMatch(img -> img.getQuantity() != null && img.getQuantity() > 0 && img.getQuantity() < 3);
            
            if (hasLowStock) {
                try {
                    emailService.sendLowStockAlert(product);
                } catch (Exception e) {
                    logger.error("Failed to send low stock alert email for product {}: {}", product.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse imageDetails for low stock check for product {}: {}", product.getId(), e.getMessage());
        }
    }

    // ✅ Soft delete
    public int deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with ID " + id + " not found"));

        product.setActive(false);
        productRepository.save(product);
        return 1;
    }

    // ✅ Hard delete
    public int hardDeleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product with ID " + id + " not found");
        }
        productRepository.deleteById(id);
        return 1;
    }
}
