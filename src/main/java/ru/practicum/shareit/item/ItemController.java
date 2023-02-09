package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

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

        return service.save(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(HEADER_OWNER_ID) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Handling a request to update the item with id {} for owner with id {}", itemId, ownerId);

        return service.update(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(HEADER_OWNER_ID) Long userId,
                           @PathVariable Long itemId) {
        log.info("Handling a request to get an item with id {}", itemId);

        return service.getDtoById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllOwnersItems(@RequestHeader(HEADER_OWNER_ID) Long ownerId) {
        log.info("Handling get all items for owner with id {}", ownerId);

        return service.getAllDto(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByText(@RequestHeader(HEADER_OWNER_ID) Long userId,
                                         @RequestParam String text) {
        log.info("Processing a request to search for an item by text: {}", text);

        return service.findDtoByText(text, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto saveNewComment(@RequestHeader(HEADER_OWNER_ID) Long authorId,
                                     @PathVariable Long itemId,
                                     @Valid @RequestBody CommentDto commentDto) {
        log.info("Handling a request to create a new comment for item id {} by author with id {}", itemId, authorId);

        return service.saveCommentDto(commentDto, authorId, itemId);
    }

}
