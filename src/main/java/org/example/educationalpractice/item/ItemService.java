package org.example.educationalpractice.item;

import org.example.educationalpractice.item.dto.ItemRequest;
import org.example.educationalpractice.item.dto.ItemResponse;
import org.example.educationalpractice.item.dto.ItemSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {

    ItemResponse create(ItemRequest request);

    ItemResponse getById(Long id);

    Page<ItemResponse> search(ItemSearchCriteria criteria, Pageable pageable);

    ItemResponse update(Long id, ItemRequest request);

    void delete(Long id);

    ItemResponse markAsFavorite(Long id);

    ItemResponse removeFromFavorites(Long id);

    List<ItemResponse> getFavorites();
}
