package org.example.educationalpractice.basket;

import org.example.educationalpractice.basket.dto.BasketResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @GetMapping
    public BasketResponse getBasket() {
        return basketService.getBasket();
    }

    @PostMapping("/items/{itemId}")
    public BasketResponse addItem(@PathVariable Long itemId) {
        return basketService.addItem(itemId);
    }

    @DeleteMapping("/items/{itemId}")
    public BasketResponse removeItem(@PathVariable Long itemId) {
        return basketService.removeItem(itemId);
    }
}
