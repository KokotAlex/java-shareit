package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private static final Map<Long, Item> items = new HashMap<>();

    private static Long autoincrement = 0L;

    @Override
    public Long save(Long ownerId, Item item) {
        item.setId(++autoincrement);
        item.setOwnerId(ownerId);

        Long itemId = item.getId();
        items.put(itemId, item);

        log.debug("Saving item with id {} for user with id {}",  itemId, ownerId);

        return itemId;
    }

    @Override
    public Optional<Item> findById(Long itemId) {

        Optional<Item> optionalItem;

        if (items.containsKey(itemId)) {
            optionalItem = Optional.of(items.get(itemId));
        } else {
            optionalItem = Optional.empty();
        }

        log.debug("Result finding item by id {}: {}",  itemId, optionalItem);

        return optionalItem;
    }

    @Override
    public void update(Long itemId, Item item) {
        Item updatedItem = items.get(itemId);

        String name = item.getName();
        Boolean available = item.getAvailable();
        String description = item.getDescription();

        if (name != null) {
            updatedItem.setName(name);
        }

        if (available != null) {
            updatedItem.setAvailable(available);
        }

        if (description != null) {
            updatedItem.setDescription(description);
        }

        log.debug("Updated item with id {}", itemId);
    }

    @Override
    public List<Item> findAll(Long ownerId) {
        List<Item> itemList = items.values().stream().
                filter(item -> Objects.equals(item.getOwnerId(), ownerId)).
                collect(Collectors.toList());

        log.debug("Finding all items for owner with id {}", ownerId);

        return itemList;
    }

    @Override
    public List<Item> findByText(String text) {
        String lowerText = text.toLowerCase();
        List<Item> itemList = items.values().stream().
                filter(item ->
                        (item.getName().toLowerCase().contains(lowerText))
                                || (item.getDescription().toLowerCase().contains(lowerText))).
                filter(Item::getAvailable).
                collect(Collectors.toList());

        log.debug("Finding all items with text: {}", text);

        return itemList;
    }
}
