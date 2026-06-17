package org.example.educationalpractice.basket.dto;

import org.example.educationalpractice.item.dto.ItemResponse;

import java.math.BigDecimal;
import java.util.List;

public record BasketResponse(
        Long id,
        List<ItemResponse> items,
        int itemCount,
        BigDecimal totalPrice
) {
}
