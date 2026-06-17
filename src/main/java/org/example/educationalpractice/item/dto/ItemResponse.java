package org.example.educationalpractice.item.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record ItemResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        Set<String> tags,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt
) {
}
