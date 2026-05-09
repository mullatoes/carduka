package com.carduka.dto;

import java.math.BigDecimal;

public record CarResponse (
        Long id,
        String make,
        String model,
        Integer manufactureYear,
        String color,
        BigDecimal price
) {
}
