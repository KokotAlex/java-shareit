package ru.practicum.shareit.item.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    public final ItemRepository itemRepository;
    public final CommentRepository commentRepository;
    public final UserService userService;

    @Override
    @Transactional
    public ItemDto save(Long ownerId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item savedItem = save(ownerId, item);

        return ItemMapper.toItemDto(savedItem, ownerId);
    }
    @Override
    public Item getById(Long itemId) {
        log.info("Start getting item by id {}", itemId);

        Item foundedItem = itemRepository.findById(itemId)
                        .orElseThrow(() -> new NotFoundException(Item.class.getSimpleName(), itemId));

        log.info("Finish getting item by id {}", itemId);

        return foundedItem;
    }

    @Override
    public ItemDto getDtoById(Long itemId, Long userId) {
        Item item = getById(itemId);

        return ItemMapper.toItemDto(item, userId);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        Item updatedItem = update(ownerId, itemId, item);

        return ItemMapper.toItemDto(updatedItem, ownerId);
    }

    @Override
    public List<ItemDto> getAllDto(Long ownerId) {

        return getAll(ownerId).stream()
                .map(item -> ItemMapper.toItemDto(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findDtoByText(String text, Long userId) {
        return findByText(text).stream()
                .map(item -> ItemMapper.toItemDto(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto saveCommentDto(CommentDto commentDto, Long authorId, Long itemId) {
        Comment comment = ItemMapper.toComment(commentDto);
        Comment savedComment = saveComment(comment, authorId, itemId);

        return ItemMapper.toCommentDto(savedComment);
    }

    private Item save(Long ownerId, Item item) {
        log.info("Start saving item {}", item);

        User owner = userService.getById(ownerId);

        item.setOwner(owner);
        Item newItem = itemRepository.save(item);

        log.info("Finish saving user {}", item);

        return newItem;
    }

    private Item update(Long ownerId, Long itemId, Item item) {
        log.info("Start updating item by id {} for owner with id {}", itemId, ownerId);

        User currentOwner = userService.getById(ownerId);
        Item itemForUpdate = getById(itemId);

        // Проверим, что переданный владелец действительно владеет вещью.
        if (!currentOwner.equals(itemForUpdate.getOwner())) {
            throw new NotFoundException("User with id " + ownerId + " doesn't own the item with id " + itemId);
        }

        // Обновим вещь.
        String name = item.getName();
        Boolean available = item.getAvailable();
        String description = item.getDescription();

        if (name != null) {
            itemForUpdate.setName(name);
        }

        if (available != null) {
            itemForUpdate.setAvailable(available);
        }

        if (description != null) {
            itemForUpdate.setDescription(description);
        }

        // Запишем обновленную вещь.
        Item updatedItem = itemRepository.save(itemForUpdate);

        log.info("Finish updating item by id {} for owner with id {}", itemId, ownerId);

        return updatedItem;
    }

    private List<Item> getAll(Long ownerId) {
        log.info("Start getting all items for owner with id {}", ownerId);

        User owner = userService.getById(ownerId);
        List<Item> items = itemRepository.findByOwnerOrderById(owner);

        log.info("Finish getting all items for owner with id {}", ownerId);

        return items;
    }

    private List<Item> findByText(String text) {
        log.info("Start getting all items with text: {}", text);

        if (text.isBlank()) {
            return new ArrayList<>();
        }

        // Сформируем условия к запросу.
        BooleanExpression byName = QItem.item.name.containsIgnoreCase(text);
        BooleanExpression byDescription = QItem.item.description.containsIgnoreCase(text);
        BooleanExpression byAvailable = QItem.item.available.eq(true);
        // Выполним запрос.
        Iterable<Item> foundItems = itemRepository.findAll(byAvailable.and(byName.or(byDescription)));

        // Сформируем коллекцию.
        List<Item> items = new ArrayList<>();
        foundItems.forEach(items::add);

        log.info("Finish getting all items with text: {}", text);

        return items;
    }

    private Comment saveComment(Comment comment, Long authorId, Long itemId) {

        Item item = getById(itemId);
        // Проверим, заказывал ли данный пользователь текущую вещь.
        boolean isItBooker = item.getBookings().stream()
                .filter(booking -> booking.getBooker().getId().equals(authorId))
                .anyMatch(booking -> booking.getStart().isBefore(LocalDateTime.now()));
        if (!isItBooker) {
            throw new BadRequestException("Leave a comment on the product can only be the customer of the product");
        }

        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        User author = userService.getById(authorId);
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }
}
