package ru.practicum.shareit.item.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceImplTest {

    ItemService itemService;
    ItemRepository itemRepository;
    CommentRepository commentRepository;
    UserService userService;
    RequestService requestService;

    @Captor
    ArgumentCaptor<Item> itemCaptor;

    @Captor
    ArgumentCaptor<Comment> commentCaptor;

    final Long userId = 1L;
    final Long requestId = 1L;
    final User owner = User.builder()
            .id(userId)
            .email("owner@email.com")
            .name("owner")
            .build();

    final Item item = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .owner(owner)
            .request(ItemRequest.builder().id(requestId).build())
            .build();

    @BeforeEach
    void beforeEach() {
        itemRepository = mock(ItemRepository.class);
        commentRepository = mock(CommentRepository.class);
        userService = mock(UserService.class);
        requestService = mock(RequestService.class);
        itemService = new ItemServiceImpl(itemRepository, commentRepository, userService, requestService);
    }

    @Test
    void save_whenSaveItemWithExactingUserAndRequest_thenReturnItemTest() {
        // Этап 1. Подготовка.
        User anotherOwner = User.builder().id(2L).build();
        ItemRequest anotherRequest = ItemRequest.builder().id(2L).build();
        Item expectedItem = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(anotherOwner)
                .request(anotherRequest)
                .build();

        when(requestService.getById(anotherRequest.getId())).thenReturn(anotherRequest);
        when(userService.getById(anotherOwner.getId())).thenReturn(anotherOwner);
        when(itemRepository.save(any())).thenReturn(expectedItem);

        // Этап 2. Выполнение.
        Item returnedItem = itemService.save(anotherOwner.getId(), item, anotherRequest.getId());

        // Этап 3. Проверка.
        verify(itemRepository).save(itemCaptor.capture());
        Item savedItem = itemCaptor.getValue();

        // Проверим, что сохраненное значение равно ожидаемому.
        assertEquals(expectedItem.getId(), savedItem.getId());
        assertEquals(expectedItem.getName(), savedItem.getName());
        assertEquals(expectedItem.getDescription(), savedItem.getDescription());
        assertEquals(expectedItem.getAvailable(), savedItem.getAvailable());
        assertEquals(expectedItem.getOwner(), savedItem.getOwner());
        assertEquals(expectedItem.getRequest(), savedItem.getRequest());

        // Проверим, что возвращенное значение равно ожидаемому.
        assertEquals(expectedItem.getId(), returnedItem.getId());
        assertEquals(expectedItem.getName(), returnedItem.getName());
        assertEquals(expectedItem.getDescription(), returnedItem.getDescription());
        assertEquals(expectedItem.getAvailable(), returnedItem.getAvailable());
        assertEquals(expectedItem.getOwner(), returnedItem.getOwner());
        assertEquals(expectedItem.getRequest(), returnedItem.getRequest());

        // Проверим наличие вызовов.
        verify(requestService, times(1))
                .getById(anotherRequest.getId());
        verify(userService, times(1))
                .getById(anotherOwner.getId());
        verify(itemRepository, times(1))
                .save(savedItem);
    }

    @Test
    void getById_WhenItemFound_thenReturnedItemTest() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        final Item foundedItem = itemService.getById(item.getId());

        assertNotNull(foundedItem);
        assertEquals(item, foundedItem);
        verify(itemRepository, times(1))
                .findById(item.getId());
    }

    @Test
    void getById_WhenItemNotFound_thenNotFoundExceptionThrownTest() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(item.getId()));
        verify(itemRepository, times(1))
                .findById(item.getId());
    }

    @Test
    void update_WhenUserExistAndItemDetailsCompleted_ThenItemReturnWithUpdatedDetailsTest() {
        // Этап 1. Подготовка.
        ItemRequest anotherRequest = ItemRequest.builder().id(2L).build();
        Item itemDataForUpdate = Item.builder()
                .name("ItemUpdate")
                .description("DescriptionUpdate")
                .available(false)
                .build();
        Item expectedItem = Item.builder()
                .id(item.getId())
                .name(itemDataForUpdate.getName())
                .description(itemDataForUpdate.getDescription())
                .available(itemDataForUpdate.getAvailable())
                .owner(item.getOwner())
                .request(anotherRequest)
                .build();
        when(userService.getById(item.getOwner().getId())).thenReturn(item.getOwner());
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(requestService.getById(anotherRequest.getId())).thenReturn(anotherRequest);
        when(itemRepository.save(any())).thenReturn(expectedItem);

        // Этап 2. Выполнение.
        Item returnedItem = itemService.update(item.getOwner().getId(), item.getId(), anotherRequest.getId(), itemDataForUpdate);

        // Этап 3. Проверка.
        verify(itemRepository).save(itemCaptor.capture());
        Item savedItem = itemCaptor.getValue();

        // Проверим, что возвращаемое значение равно ожидаемому.
        assertEquals(expectedItem.getId(), returnedItem.getId());
        assertEquals(expectedItem.getName(), returnedItem.getName());
        assertEquals(expectedItem.getDescription(), returnedItem.getDescription());
        assertEquals(expectedItem.getAvailable(), returnedItem.getAvailable());
        assertEquals(expectedItem.getOwner(), returnedItem.getOwner());
        assertEquals(expectedItem.getRequest(), returnedItem.getRequest());

        // Проверим, что сохраняемое значение равно ожидаемому.
        assertEquals(expectedItem.getId(), savedItem.getId());
        assertEquals(expectedItem.getName(), savedItem.getName());
        assertEquals(expectedItem.getDescription(), savedItem.getDescription());
        assertEquals(expectedItem.getAvailable(), savedItem.getAvailable());
        assertEquals(expectedItem.getOwner(), savedItem.getOwner());
        assertEquals(expectedItem.getRequest(), savedItem.getRequest());

        // Проверим вызовы.
        verify(userService, times(1)).getById(item.getOwner().getId());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(requestService, times(1)).getById(anotherRequest.getId());
        verify(itemRepository, times(1)).save(savedItem);
    }

    @Test
    void getAll_whenOwnerExist_thenReturnOwnersItemsCollectionTest() {
        when(userService.getById(item.getOwner().getId())).thenReturn(item.getOwner());
        when(itemRepository.findByOwnerOrderById(item.getOwner())).thenReturn(List.of(item));

        final List<Item> items = itemService.getAll(item.getOwner().getId());

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));

        InOrder inOrder = inOrder(userService, itemRepository);
        inOrder.verify(userService, times(1)).getById(item.getOwner().getId());
        inOrder.verify(itemRepository, times(1))
                .findByOwnerOrderById(item.getOwner());
    }

    @Test
    void findByText_whenTextIsPresent_thenReturnItemCollectionTest() {
        Iterable<Item> foundItems = List.of(item);
        when(itemRepository.findAll(any(BooleanExpression.class))).thenReturn(foundItems);

        List<Item> returnedItems = itemService.findByText(item.getName());

        assertNotNull(returnedItems);
        assertEquals(1, returnedItems.size());
        assertEquals(item, returnedItems.get(0));

        verify(itemRepository, times(1)).findAll(any(BooleanExpression.class));
    }

    @Test
    void findByText_whenTextIsEmpty_thenReturnEmptyCollectionTest() {
        List<Item> returnedItems = itemService.findByText("");

        assertNotNull(returnedItems);
        assertTrue(returnedItems.isEmpty());

        verify(itemRepository, never()).findAll(any(BooleanExpression.class));
    }

    @Test
    void saveComment_whenItemAndBookerExist_thenReturnCommentTest() {
        // Этап 1. Подготовка.
        final LocalDateTime time = of(2020, 1, 1, 0, 0, 1);
        Comment commentToSave = Comment.builder()
                .created(time)
                .text("Comment")
                .build();
        Comment expectedComment = Comment.builder()
                .id(1L)
                .created(commentToSave.getCreated())
                .text(commentToSave.getText())
                .author(owner)
                .item(item)
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(time)
                .booker(owner)
                .build();
        item.getBookings().add(booking);

        when(itemRepository.findById(expectedComment.getItem().getId()))
                .thenReturn(Optional.of(expectedComment.getItem()));
        when(userService.getById(expectedComment.getAuthor().getId()))
                .thenReturn(expectedComment.getAuthor());
        when(commentRepository.save(any())).thenReturn(expectedComment);

        // Этап 2. Выполнение.
        Comment returnedComment = itemService.saveComment(commentToSave,
                expectedComment.getAuthor().getId(),
                expectedComment.getItem().getId());

        // Этап 3. Проверка.
        verify(commentRepository).save(commentCaptor.capture());
        Comment savedComment = commentCaptor.getValue();

        // Проверим, что возвращаемое значение соответствует ожидаемому.
        assertEquals(expectedComment.getId(), returnedComment.getId());
        assertEquals(expectedComment.getCreated(), returnedComment.getCreated());
        assertEquals(expectedComment.getText(), returnedComment.getText());
        assertEquals(expectedComment.getAuthor(), returnedComment.getAuthor());
        assertEquals(expectedComment.getItem(), returnedComment.getItem());

        // Проверим, что сохраняемое значение соответствует ожидаемому.
        assertEquals(expectedComment.getText(), savedComment.getText());
        assertEquals(expectedComment.getAuthor(), savedComment.getAuthor());
        assertEquals(expectedComment.getItem(), savedComment.getItem());

        // Проверим вызовы.
        InOrder inOrder = inOrder(itemRepository, userService, commentRepository);
        inOrder.verify(itemRepository, times(1))
                .findById(expectedComment.getItem().getId());
        inOrder.verify(userService, times(1))
                .getById(expectedComment.getAuthor().getId());
        inOrder.verify(commentRepository, times(1))
                .save(savedComment);
    }

    @Test
    void saveComment_whenItemIsNotExist_thenNotFoundExceptionThrownTest() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.saveComment(new Comment(), anyLong(), item.getId()));
        verify(itemRepository, times(1))
                .findById(item.getId());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void saveComment_whenBookerIsNotExist_thenBadRequestExceptionThrownTest() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        item.getBookings().clear();

        assertThrows(BadRequestException.class,
                () -> itemService.saveComment(new Comment(), anyLong(), item.getId()));
        verify(itemRepository, times(1))
                .findById(item.getId());
        verify(commentRepository, never()).save(any());
    }
}