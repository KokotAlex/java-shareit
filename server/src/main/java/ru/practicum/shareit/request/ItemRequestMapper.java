package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;

import java.util.Set;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .created(itemRequestDto.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, Long userId) {
        Set<ItemDto.Nested> items = itemRequest.getItems().stream()
                .map(item -> ItemMapper.toItemDtoNested(item, userId))
                .collect(Collectors.toSet());
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description((itemRequest.getDescription()))
                .requestor(UserMapper.toUserDtoShort(itemRequest.getRequestor()))
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }

}
