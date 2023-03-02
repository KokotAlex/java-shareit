package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    public final RequestService service;

    @PostMapping
    public ItemRequestDto saveNewRequest(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                         @RequestBody ItemRequestDto itemRequestDto) {

        log.info("Handling a request to create a new request for user with id {}", ownerId);

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto);
        ItemRequest savedRequest = service.save(ownerId, request);

        return ItemRequestMapper.toItemRequestDto(savedRequest, ownerId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUsersRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Handling get all requests for user with id {}", userId);

        return service.getAllUsersRequests(userId).stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(itemRequest, userId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @PathVariable Long requestId) {
        log.info("Handling get request by id {}", requestId);

        ItemRequest itemRequest = service.getById(requestId, userId);

        return ItemRequestMapper.toItemRequestDto(itemRequest, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(HEADER_USER_ID) Long userId,
                                       @RequestParam(defaultValue = "0") Integer from,
                                       @RequestParam(defaultValue = "30") Integer size) {
        log.info("Handling get all requests");

        ItemRequestRequestParam params = ItemRequestRequestParam.builder()
                .from(from)
                .size(size)
                .build();

        return service.getAll(userId, params).stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(itemRequest, userId))
                .collect(Collectors.toList());
    }

}
