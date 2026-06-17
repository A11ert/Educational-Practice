package org.example.educationalpractice.item;

import org.example.educationalpractice.item.dto.ItemResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FavoriteController {

    private final ItemService itemService;

    public FavoriteController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PutMapping("/items/{id}/favorite")
    public ItemResponse markAsFavorite(@PathVariable Long id) {
        return itemService.markAsFavorite(id);
    }

    @DeleteMapping("/items/{id}/favorite")
    public ItemResponse removeFromFavorites(@PathVariable Long id) {
        return itemService.removeFromFavorites(id);
    }

    @GetMapping("/favorites")
    public List<ItemResponse> getFavorites() {
        return itemService.getFavorites();
    }
}
