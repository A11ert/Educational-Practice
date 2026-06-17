package org.example.educationalpractice.item.dto;

import java.math.BigDecimal;
import java.util.Set;

public record ItemSearchCriteria(
        String keyword,
        Set<String> tags,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
