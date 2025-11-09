package com.chada.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesDTO {
    private String month; // Format: "YYYY-MM" or "January 2024" or "Monday 15/01"
    private String date; // Format: "YYYY-MM-DD" for weekly data
    private BigDecimal totalSales;
    private int orderCount;
}

