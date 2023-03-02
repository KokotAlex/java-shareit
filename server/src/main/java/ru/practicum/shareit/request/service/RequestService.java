package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.ItemRequestRequestParam;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface RequestService {

    ItemRequest save(Long ownerId, ItemRequest request);

    List<ItemRequest> getAllUsersRequests(Long userId);

    List<ItemRequest> getAll(Long userId, ItemRequestRequestParam params);

    ItemRequest getById(Long requestId);

    ItemRequest getById(Long requestId, Long userId);
}
