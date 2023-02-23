package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequestRequestParam;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    public final RequestRepository requestRepository;

    public final UserService userService;

    @Override
    public ItemRequest getById(Long requestId) {
        log.info("Start getting item request by id {}", requestId);

        ItemRequest foundedItemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ItemRequest.class.getSimpleName(), requestId));

        log.info("Finish getting item request by id {}", requestId);

        return foundedItemRequest;
    }

    @Override
    public ItemRequest getById(Long requestId, Long userId) {
        log.info("Start getting item request by id {} for user with id {}", requestId, userId);

        userService.checkUserExist(userId);
        ItemRequest foundedItemRequest = getById(requestId);

        log.info("Finish getting item request by id {} for user with id {}", requestId, userId);

        return foundedItemRequest;
    }

    @Override
    @Transactional
    public ItemRequest save(Long ownerId, ItemRequest request) {
        log.info("Start saving item request with id {} for user with id {}", request.getId(), ownerId);

        User owner = userService.getById(ownerId);

        request.setRequestor(owner);
        request.setCreated(LocalDateTime.now());
        ItemRequest newRequest = requestRepository.save(request);

        log.info("Finish saving item request with id {} for user with id {}", request.getId(), ownerId);

        return newRequest;
    }

    @Override
    public List<ItemRequest> getAllUsersRequests(Long userId) {
        log.info("Start getting all requests for user with id {}", userId);

        userService.checkUserExist(userId);
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        log.info("Finish getting all requests for user with id {}", userId);

        return requests;
    }

    @Override
    public List<ItemRequest> getAll(Long userId, ItemRequestRequestParam params) {
        log.info("Start getting all request");

        PageRequest pr = PageRequest.of(params.getFrom(), params.getSize());
        Page<ItemRequest> page = requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pr);
        List<ItemRequest> requests = page.getContent();

        log.info("Finish getting all request");

        return requests;
    }

}
