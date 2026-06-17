package org.example.educationalpractice.basket;

import org.example.educationalpractice.basket.dto.BasketResponse;
import org.example.educationalpractice.common.exception.ResourceNotFoundException;
import org.example.educationalpractice.item.Item;
import org.example.educationalpractice.item.ItemMapper;
import org.example.educationalpractice.item.ItemRepository;
import org.example.educationalpractice.item.dto.ItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class BasketServiceImpl implements BasketService {

    private final BasketRepository basketRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public BasketServiceImpl(BasketRepository basketRepository, ItemRepository itemRepository, ItemMapper itemMapper) {
        this.basketRepository = basketRepository;
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    public BasketResponse getBasket() {
        return toResponse(getOrCreateBasket());
    }

    @Override
    public BasketResponse addItem(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", itemId);
        }
        Basket basket = getOrCreateBasket();
        basket.addItem(itemId);
        return toResponse(basketRepository.save(basket));
    }

    @Override
    public BasketResponse removeItem(Long itemId) {
        Basket basket = getOrCreateBasket();
        basket.removeItem(itemId);
        return toResponse(basketRepository.save(basket));
    }

    private Basket getOrCreateBasket() {
        return basketRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> basketRepository.save(new Basket()));
    }

    private BasketResponse toResponse(Basket basket) {
        List<Item> items = itemRepository.findAllById(basket.getItemIds());
        List<ItemResponse> itemResponses = items.stream()
                .map(itemMapper::toResponse)
                .toList();
        BigDecimal totalPrice = items.stream()
                .map(Item::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new BasketResponse(basket.getId(), itemResponses, itemResponses.size(), totalPrice);
    }
}
