package com.chada.repository;

import com.chada.entity.Product;
import com.chada.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(Category category);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    List<Product> findByCategoryIdAndActiveTrue(@Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.active = true")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.discountPercentage > 0 " +
           "AND (p.promotionStartDate IS NULL OR p.promotionStartDate <= :today) " +
           "AND (p.promotionEndDate IS NULL OR p.promotionEndDate >= :today)")
    List<Product> findActivePromotions(@Param("today") LocalDate today);
}
