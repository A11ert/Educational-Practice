package org.example.educationalpractice.item;

import org.example.educationalpractice.item.dto.ItemRequest;
import org.example.educationalpractice.item.dto.ItemResponse;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class ItemMapper {

    public Item toEntity(ItemRequest request) {
        Item item = new Item();
        applyRequest(item, request);
        return item;
    }

    public void applyRequest(Item item, ItemRequest request) {
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setTags(copyTags(request.tags()));
    }

    public ItemResponse toResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                copyTags(item.getTags()),
                item.isFavorite(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private Set<String> copyTags(Set<String> tags) {
        return tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
    }
}
