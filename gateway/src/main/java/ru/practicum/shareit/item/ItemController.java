package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.ArrayList;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemController {

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> saveNewItem(@RequestHeader(HEADER_USER_ID) Long ownerId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Handling a request to create a new item for owner with id {}", ownerId);

        return itemClient.saveNewItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(HEADER_USER_ID) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Handling a request to update the item with id {} for owner with id {}", itemId, ownerId);

        return itemClient.update(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(HEADER_USER_ID) Long userId,
                           @PathVariable Long itemId) {
        log.info("Handling a request to get an item with id {}", itemId);

        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwnersItems(@RequestHeader(HEADER_USER_ID) Long ownerId) {
        log.info("Handling get all items for owner with id {}", ownerId);

        return itemClient.getAllOwnersItems(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItemsByText(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @RequestParam String text) {
        log.info("Processing a request to search for an item by text: {}", text);

        if (text.isBlank()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        return itemClient.findItemsByText(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> saveNewComment(@RequestHeader(HEADER_USER_ID) Long authorId,
                                     @PathVariable Long itemId,
                                     @Valid @RequestBody CommentDto commentDto) {
        log.info("Handling a request to create a new comment for item id {} by author with id {}", itemId, authorId);

       return itemClient.saveNewComment(authorId, itemId, commentDto);
    }

}
