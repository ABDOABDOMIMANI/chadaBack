package com.chada.service;

import com.chada.entity.Category;
import com.chada.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Retrieve all categories from the database.
     */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    /**
     * Find a category by ID.
     */
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
    }

    /**
     * Create a new category.
     */
    public Category create(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    /**
     * Update an existing category.
     */
    public Category update(Long id, Category updatedCategory) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));

        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        existing.setImageUrl(updatedCategory.getImageUrl());

        return categoryRepository.save(existing);
    }


    /**
     * Delete a category by ID.
     */
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category with ID " + id + " not found");
        }
        categoryRepository.deleteById(id);
    }
}
