package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private static final String HEADER_OWNER_ID = "X-Sharer-User-Id";

    public final ItemService service;

    @PostMapping
    public ItemDto saveNewItem(@RequestHeader(HEADER_OWNER_ID) Long ownerId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Handling a request to create a new item for owner with id {}", ownerId);

        Item item = ItemMapper.toItem(itemDto);
        Item savedItem = service.save(ownerId, item);

        return ItemMapper.toItemDto(savedItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(HEADER_OWNER_ID) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Handling a request to update the item with id {} for owner with id {}", itemId, ownerId);

        Item item = ItemMapper.toItem(itemDto);
        Item updatedItem = service.update(ownerId, itemId, item);

        return ItemMapper.toItemDto(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        log.info("Handling a request to get an item with id {}", itemId);

        Item item = service.getById(itemId);

        return ItemMapper.toItemDto(item);
    }

    @GetMapping
    public List<ItemDto> getAllOwnersItems(@RequestHeader(HEADER_OWNER_ID) Long ownerId) {
        log.info("Handling get all items for owner with id {}", ownerId);

        return service.getAll(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByText(@RequestParam String text) {
        log.info("Processing a request to search for an item by text: {}", text);

        return service.findByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

}
