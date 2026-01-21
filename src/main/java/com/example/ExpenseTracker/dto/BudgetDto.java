package com.example.ExpenseTracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class BudgetDto {
    @NotBlank private String month;     // 2026-01
    @NotBlank private String category;

    @NotNull @DecimalMin("0.01")
    private BigDecimal limitAmount;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
}
