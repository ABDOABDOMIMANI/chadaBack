package com.chada.service;

import com.chada.entity.Category;
import com.chada.entity.Product;
import com.chada.repository.CategoryRepository;
import com.chada.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;

    // ✅ Get only active products
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
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
        
        // Handle promotion pricing
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
        existing.setStock(updatedProduct.getStock());
        existing.setImageUrl(updatedProduct.getImageUrl());
        existing.setImageUrls(updatedProduct.getImageUrls());
        existing.setImageDetails(updatedProduct.getImageDetails()); // New field for image details
        existing.setFragrance(updatedProduct.getFragrance());
        existing.setVolume(updatedProduct.getVolume());
        existing.setActive(updatedProduct.getActive());

        if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getId() != null) {
            Category category = categoryRepository.findById(updatedProduct.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existing.setCategory(category);
        }
        
        // Handle promotion pricing
        if (updatedProduct.getDiscountPercentage() != null && updatedProduct.getDiscountPercentage() > 0) {
            // There's a promotion
            // Set promotion fields
            existing.setDiscountPercentage(updatedProduct.getDiscountPercentage());
            existing.setPromotionStartDate(updatedProduct.getPromotionStartDate());
            existing.setPromotionEndDate(updatedProduct.getPromotionEndDate());
            
            // Original price should be provided from frontend
            if (updatedProduct.getOriginalPrice() != null) {
                existing.setOriginalPrice(updatedProduct.getOriginalPrice());
            } else {
                // Fallback: if original price not provided, use the current price as original
                // This shouldn't happen if frontend sends data correctly
                if (existing.getOriginalPrice() == null) {
                    existing.setOriginalPrice(existing.getPrice());
                }
            }
            
            // Calculate discounted price from original price
            BigDecimal discountAmount = existing.getOriginalPrice()
                    .multiply(BigDecimal.valueOf(existing.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            existing.setPrice(existing.getOriginalPrice().subtract(discountAmount));
        } else {
            // No promotion - clear promotion fields
            existing.setDiscountPercentage(null);
            existing.setPromotionStartDate(null);
            existing.setPromotionEndDate(null);
            
            // Use the price provided by the user (this is the actual price when no promotion)
            if (updatedProduct.getPrice() != null) {
                existing.setPrice(updatedProduct.getPrice());
                // Clear original price since there's no promotion
                existing.setOriginalPrice(null);
            } else {
                // If price not provided and we had a promotion, restore original price
                if (existing.getOriginalPrice() != null) {
                    existing.setPrice(existing.getOriginalPrice());
                    existing.setOriginalPrice(null);
                }
            }
        }

        Product saved = productRepository.save(existing);
        
        // Check if stock is low (less than 3) and send alert
        if (saved.getStock() != null && saved.getStock() < 3 && saved.getActive()) {
            try {
                emailService.sendLowStockAlert(saved);
            } catch (Exception e) {
                System.err.println("Failed to send low stock alert email: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return saved;
    }
    
    private void handlePromotionPricing(Product product) {
        if (product.getDiscountPercentage() != null && product.getDiscountPercentage() > 0) {
            // If original price is not set, use current price as original
            if (product.getOriginalPrice() == null) {
                product.setOriginalPrice(product.getPrice());
            }
            // Calculate discounted price
            BigDecimal discountAmount = product.getOriginalPrice()
                    .multiply(BigDecimal.valueOf(product.getDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            product.setPrice(product.getOriginalPrice().subtract(discountAmount));
        } else {
            // No promotion, clear promotion fields
            product.setDiscountPercentage(null);
            product.setOriginalPrice(null);
            product.setPromotionStartDate(null);
            product.setPromotionEndDate(null);
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
