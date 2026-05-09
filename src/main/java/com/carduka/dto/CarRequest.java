package com.carduka.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CarRequest(

        @NotBlank(message = "Make is required")
        String make,

        @NotBlank(message = "Model is required")
        String model,

        @NotNull(message = "Manufacture year is required")
        @Min(value = 1900, message = "Manufacture year must be 1900 or later")
        @Max(value = 2100, message = "Manufacture year cannot be greater than 2100")
        Integer manufactureYear,

        @NotBlank(message = "Color is required")
        String color,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0",inclusive = false, message = "Price must be greater than zero")
        BigDecimal price
) {
}
