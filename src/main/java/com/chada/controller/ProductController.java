package com.chada.controller;

import com.chada.entity.Product;
import com.chada.service.FileStorageService;
import com.chada.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://chada-admin.netlify.app", "https://chadaparfum.netlify.app", "http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Get all active products
    @GetMapping
    public List<Product> getActiveProducts() {
        return productService.getActiveProducts();
    }

    // ✅ Get all products (admin)
    @GetMapping("/admin/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    // ✅ Get products on promotion
    @GetMapping("/promotions")
    public ResponseEntity<List<Product>> getPromotions() {
        return ResponseEntity.ok(productService.getActivePromotions());
    }

    // ✅ Get single product
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ✅ Create product (accepts JSON)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok(createdProduct);
    }

    // ✅ Create product with file upload (accepts multipart/form-data)
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = {"https://chada-admin.netlify.app", "https://chadaparfum.netlify.app", "http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<Product> createProductWithImages(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            Product product = objectMapper.readValue(productJson, Product.class);
            
            // Handle image uploads
            if (images != null && images.length > 0) {
                List<String> imageUrls = fileStorageService.storeFiles(images);
                product.setImageUrls(objectMapper.writeValueAsString(imageUrls));
                if (imageUrls.size() > 0) {
                    product.setImageUrl(imageUrls.get(0)); // Set first image as main image
                }
            }
            
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            logger.error("Error creating product with images: {}", e.getMessage(), e);
            // CORS headers are handled by CorsFilter
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ✅ Update product
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    // ✅ Update product with file upload (accepts multipart/form-data)
    @PutMapping(value = "/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = {"https://chada-admin.netlify.app", "https://chadaparfum.netlify.app", "http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<Product> updateProductWithImages(
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            Product product = objectMapper.readValue(productJson, Product.class);
            
            // Handle image uploads
            if (images != null && images.length > 0) {
                List<String> imageUrls = fileStorageService.storeFiles(images);
                product.setImageUrls(objectMapper.writeValueAsString(imageUrls));
                if (imageUrls.size() > 0) {
                    product.setImageUrl(imageUrls.get(0)); // Set first image as main image
                }
            }
            
            Product updated = productService.updateProduct(id, product);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating product with images: {}", e.getMessage(), e);
            // CORS headers are handled by CorsFilter
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ✅ Hard delete (permanently delete from database)
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.hardDeleteProduct(id));
    }
}
