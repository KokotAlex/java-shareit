package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto save(Long ownerId, ItemDto itemDto);

    Item getById(Long itemId);

    ItemDto getDtoById(Long itemId, Long userId);

    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    List<ItemDto> getAllDto(Long ownerId);

    List<ItemDto> findDtoByText(String text, Long userId);

    CommentDto saveCommentDto(CommentDto commentDto, Long authorId, Long itemId);
}