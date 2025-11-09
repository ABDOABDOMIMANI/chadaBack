package com.chada.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private String url;
    private BigDecimal price; // Optional: price for this specific image/variant
    private String description; // Optional: description for this specific image/variant
    private Integer quantity; // Optional: stock quantity for this specific image/variant
}

