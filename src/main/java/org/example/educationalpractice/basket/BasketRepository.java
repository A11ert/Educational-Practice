package org.example.educationalpractice.basket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Long> {

    Optional<Basket> findFirstByOrderByIdAsc();
}
