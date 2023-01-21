package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder().
                id(item.getId()).
                name(item.getName()).
                description(item.getDescription()).
                available(item.getAvailable()).
                build();
    }

    public static Item toItem(ItemDto item) {
        return Item.builder().
                id(item.getId()).
                name(item.getName()).
                description(item.getDescription()).
                available(item.getAvailable()).
                build();
    }
}
