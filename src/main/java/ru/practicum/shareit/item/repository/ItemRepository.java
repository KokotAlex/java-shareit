package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    Long save(Long ownerId, Item item);

    Optional<Item> findById(Long itemId);

    void update(Long itemId, Item item);

    List<Item> findAll(Long ownerId);

    List<Item> findByText(String text);
}
