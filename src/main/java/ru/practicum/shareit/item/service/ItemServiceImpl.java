package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    public final ItemRepository repository;
    public final UserService userService;

    @Override
    public Item save(Long ownerId, Item item) {
        log.info("Start saving item {}", item);

        userService.checkUserExist(ownerId);

        Long itemId = repository.save(ownerId, item);
        Item newItem = getById(itemId);

        log.info("Finish saving user {}", item);

        return newItem;
    }

    @Override
    public Item getById(Long itemId) {
        log.info("Start getting item by id {}", itemId);

        Item foundedItem = repository.findById(itemId)
                        .orElseThrow(() -> new NotFoundException(Item.class.getSimpleName(), itemId));

        log.info("Finish getting item by id {}", itemId);

        return foundedItem;
    }

    @Override
    public Item update(Long ownerId, Long itemId, Item item) {
        log.info("Start updating item by id {} for owner with id {}", itemId, ownerId);

        userService.checkUserExist(ownerId);
        Item savedItem = getById(itemId);

        if (!ownerId.equals(savedItem.getOwnerId())) {
            throw new NotFoundException("User with id " + ownerId + " doesn't own the item with id " + itemId);
        }

        repository.update(itemId, item);
        Item updatedItem = getById(itemId);

        log.info("Finish updating item by id {} for owner with id {}", itemId, ownerId);

        return updatedItem;
    }

    @Override
    public List<Item> getAll(Long ownerId) {
        log.info("Start getting all items for owner with id {}", ownerId);

        userService.checkUserExist(ownerId);
        List<Item> items = repository.findAll(ownerId);

        log.info("Finish getting all items for owner with id {}", ownerId);

        return items;
    }

    @Override
    public List<Item> findByText(String text) {
        log.info("Start getting all items with text: {}", text);

        if (text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> items = repository.findByText(text);

        log.info("Finish getting all items with text: {}", text);

        return items;
    }
}
