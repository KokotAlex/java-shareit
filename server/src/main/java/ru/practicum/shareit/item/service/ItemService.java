package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item save(Long ownerId, Item item, Long requestId);

    Item getById(Long itemId);

    Item update(Long ownerId, Long itemId, Long requestId, Item item);

    List<Item> getAll(Long ownerId);

    List<Item> findByText(String text);

    Comment saveComment(Comment comment, Long authorId, Long itemId);
}