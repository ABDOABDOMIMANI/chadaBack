package com.chada.service;

import com.chada.entity.Product;
import com.chada.entity.Review;
import com.chada.repository.ProductRepository;
import com.chada.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public Review createReview(Review review) {
        if (review.getProduct() == null || review.getProduct().getId() == null) {
            throw new IllegalArgumentException("Product ID is required for a review.");
        }
        Product product = productRepository.findById(review.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + review.getProduct().getId()));

        review.setProduct(product);
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review updateReview(Long id, Review updatedReview) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with ID: " + id));

        existingReview.setCustomerName(updatedReview.getCustomerName());
        existingReview.setCustomerEmail(updatedReview.getCustomerEmail());
        existingReview.setRating(updatedReview.getRating());
        existingReview.setComment(updatedReview.getComment());

        return reviewRepository.save(existingReview);
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found with ID: " + id);
        }
        reviewRepository.deleteById(id);
    }

    public Double getAverageRatingForProduct(Long productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }

    public Long getReviewCountForProduct(Long productId) {
        return reviewRepository.countByProductId(productId);
    }
}

