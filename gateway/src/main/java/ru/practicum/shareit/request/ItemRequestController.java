package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> saveNewRequest(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {

        log.info("Handling a request to create a new request for user with id {}", ownerId);

        return itemRequestClient.saveNewRequest(ownerId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsersRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Handling get all requests for user with id {}", userId);

        return itemRequestClient.getAllUsersRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @PathVariable Long requestId) {
        log.info("Handling get request by id {}", requestId);

        return itemRequestClient.getRequestById(userId, requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(HEADER_USER_ID) Long userId,
                                       @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                       @Positive @RequestParam(defaultValue = "30") Integer size) {
        log.info("Handling get all requests");

        return itemRequestClient.getAll(userId, from, size);
    }

}
