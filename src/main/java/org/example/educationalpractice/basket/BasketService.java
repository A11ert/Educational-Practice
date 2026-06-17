package org.example.educationalpractice.basket;

import org.example.educationalpractice.basket.dto.BasketResponse;

public interface BasketService {

    BasketResponse getBasket();

    BasketResponse addItem(Long itemId);

    BasketResponse removeItem(Long itemId);
}
