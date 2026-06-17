package org.example.educationalpractice.item;

import org.example.educationalpractice.common.exception.ResourceNotFoundException;
import org.example.educationalpractice.item.dto.ItemRequest;
import org.example.educationalpractice.item.dto.ItemResponse;
import org.example.educationalpractice.item.dto.ItemSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional
    public ItemResponse create(ItemRequest request) {
        Item item = itemMapper.toEntity(request);
        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    public ItemResponse getById(Long id) {
        return itemMapper.toResponse(getItemOrThrow(id));
    }

    @Override
    public Page<ItemResponse> search(ItemSearchCriteria criteria, Pageable pageable) {
        validatePriceRange(criteria);
        Specification<Item> specification = ItemSpecifications.fromCriteria(criteria);
        return itemRepository.findAll(specification, pageable).map(itemMapper::toResponse);
    }

    @Override
    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = getItemOrThrow(id);
        itemMapper.applyRequest(item, request);
        return itemMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Item item = getItemOrThrow(id);
        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public ItemResponse markAsFavorite(Long id) {
        return setFavorite(id, true);
    }

    @Override
    @Transactional
    public ItemResponse removeFromFavorites(Long id) {
        return setFavorite(id, false);
    }

    @Override
    public List<ItemResponse> getFavorites() {
        return itemRepository.findByFavoriteTrueOrderByCreatedAtDesc().stream()
                .map(itemMapper::toResponse)
                .toList();
    }

    private ItemResponse setFavorite(Long id, boolean favorite) {
        Item item = getItemOrThrow(id);
        item.setFavorite(favorite);
        return itemMapper.toResponse(itemRepository.save(item));
    }

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    private void validatePriceRange(ItemSearchCriteria criteria) {
        if (criteria.minPrice() != null && criteria.maxPrice() != null
                && criteria.minPrice().compareTo(criteria.maxPrice()) > 0) {
            throw new IllegalArgumentException("minPrice must not be greater than maxPrice");
        }
    }
}
