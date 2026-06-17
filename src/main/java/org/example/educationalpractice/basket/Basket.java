package org.example.educationalpractice.basket;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;


@Entity
@Table(name = "baskets")
@Getter
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "basket_items", joinColumns = @JoinColumn(name = "basket_id"))
    @Column(name = "item_id")
    private Set<Long> itemIds = new LinkedHashSet<>();

    public void addItem(Long itemId) {
        itemIds.add(itemId);
    }

    public void removeItem(Long itemId) {
        itemIds.remove(itemId);
    }
}
