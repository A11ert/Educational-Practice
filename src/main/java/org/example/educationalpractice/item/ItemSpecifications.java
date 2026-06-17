package org.example.educationalpractice.item;

import jakarta.persistence.criteria.JoinType;
import org.example.educationalpractice.item.dto.ItemSearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Builds a single query from the filters that were actually provided.
// Each filter is its own small Specification so they can be combined freely.
public final class ItemSpecifications {

    private ItemSpecifications() {
    }

    public static Specification<Item> fromCriteria(ItemSearchCriteria criteria) {
        List<Specification<Item>> filters = new ArrayList<>();
        if (StringUtils.hasText(criteria.keyword())) {
            filters.add(keywordMatches(criteria.keyword()));
        }
        if (criteria.tags() != null && !criteria.tags().isEmpty()) {
            filters.add(hasAnyTag(criteria.tags()));
        }
        if (criteria.minPrice() != null) {
            filters.add(priceAtLeast(criteria.minPrice()));
        }
        if (criteria.maxPrice() != null) {
            filters.add(priceAtMost(criteria.maxPrice()));
        }
        return Specification.allOf(filters);
    }

    private static Specification<Item> keywordMatches(String keyword) {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("title")), pattern),
                builder.like(builder.lower(root.get("description")), pattern)
        );
    }

    private static Specification<Item> hasAnyTag(Collection<String> tags) {
        return (root, query, builder) -> {
            query.distinct(true);
            return root.join("tags", JoinType.LEFT).in(tags);
        };
    }

    private static Specification<Item> priceAtLeast(BigDecimal minPrice) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<Item> priceAtMost(BigDecimal maxPrice) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
