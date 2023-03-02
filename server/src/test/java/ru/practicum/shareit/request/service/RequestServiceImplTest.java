package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequestRequestParam;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@MockitoSettings(strictness = Strictness.LENIENT)
class RequestServiceImplTest {

    RequestRepository requestRepository;
    UserService userService;
    RequestService requestService;

    @Captor
    ArgumentCaptor<ItemRequest> requestArgumentCaptor;

    final LocalDateTime time = of(2020, 1, 1, 0, 0, 1);
    final Long userId = 1L;
    final User requestor = User.builder()
            .id(userId)
            .email("requestor@email.com")
            .name("requestor")
            .build();
    final ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .description("ItemDescription")
            .created(time)
            .requestor(requestor)
            .build();

    @BeforeEach
    void beforeEach() {
        userService = mock(UserService.class);
        requestRepository = mock(RequestRepository.class);
        requestService = new RequestServiceImpl(requestRepository, userService);
    }

    @Test
    void getById_WhenRequestFound_thenReturnedRequestTest() {
        when(requestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));

        final ItemRequest foundedItemRequest = requestService.getById(itemRequest.getId());

        assertNotNull(foundedItemRequest);
        assertEquals(itemRequest, foundedItemRequest);
        verify(requestRepository, times(1))
                .findById(itemRequest.getId());
    }

    @Test
    void getById_WhenRequestNotFound_thenNotFoundExceptionThrownTest() {
        when(requestRepository.findById(itemRequest.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getById(itemRequest.getId()));
        verify(requestRepository, times(1))
                .findById(itemRequest.getId());
    }

    @Test
    void getById_WhenUserExist_thenReturnedRequestTest() {
        when(requestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));

        final ItemRequest foundedItemRequest = requestService.getById(itemRequest.getId(), userId);

        assertNotNull(foundedItemRequest);
        assertEquals(itemRequest, foundedItemRequest);

        InOrder inOrder = inOrder(userService, requestRepository);

        inOrder.verify(userService, times(1))
                .checkUserExist(userId);
        inOrder.verify(requestRepository, times(1))
                .findById(itemRequest.getId());
    }

    @Test
    void getById_WhenUserIsNotExist_thenNotFoundExceptionThrownTest() {
        final Long anotherUserId = 2L;
        doThrow(NotFoundException.class).when(userService).checkUserExist(anotherUserId);

        assertThrows(NotFoundException.class, () -> requestService.getById(itemRequest.getId(), anotherUserId));

        verify(userService, times(1))
                .checkUserExist(anotherUserId);
        verify(requestRepository, never())
                .findById(itemRequest.getId());
    }

    @Test
    void save_whenSaveRequestWithExactingUser_thenReturnRequestTest() {
        final Long anotherUserId = 2L;
        final User requestorToSave = User.builder()
                .id(anotherUserId)
                .email("requestor@email.com")
                .name("requestor")
                .build();
        final ItemRequest itemRequestToSave = ItemRequest.builder()
                .description("ItemDescription")
                .build();
        final ItemRequest expectedItemRequest = ItemRequest.builder()
                .id(2L)
                .description("ItemDescription")
                .created(time)
                .requestor(requestorToSave)
                .build();
        when(userService.getById(anotherUserId)).thenReturn(requestorToSave);
        when(requestRepository.save(any())).thenReturn(expectedItemRequest);

        final ItemRequest returnedRequest = requestService.save(anotherUserId, itemRequestToSave);

        verify(requestRepository).save(requestArgumentCaptor.capture());
        final ItemRequest argumentRequest = requestArgumentCaptor.getValue();

        // Проверим, что сохраняемый запрос равен ожидаемому.
        assertEquals(expectedItemRequest.getDescription(), argumentRequest.getDescription());
        assertEquals(expectedItemRequest.getRequestor(), argumentRequest.getRequestor());

        // Проверим, что возвращенный из метода запрос равен ожидаемому.
        assertEquals(expectedItemRequest.getId(), returnedRequest.getId());
        assertEquals(expectedItemRequest.getDescription(), returnedRequest.getDescription());
        assertEquals(expectedItemRequest.getCreated(), returnedRequest.getCreated());
        assertEquals(expectedItemRequest.getRequestor(), returnedRequest.getRequestor());

        // Проверим наличие вызовов в БД.
        InOrder inOrder = inOrder(userService, requestRepository);
        inOrder.verify(userService, times(1))
                .getById(anotherUserId);
        inOrder.verify(requestRepository, times(1))
                .save(argumentRequest);
    }

    @Test
    void save_whenSaveRequestWithNotExactingUser_thenNotFoundExceptionThrownTest() {
        when(userService.getById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> requestService.save(userId, itemRequest));
        verify(userService, times(1)).getById(anyLong());
        verify(requestRepository, never()).save(itemRequest);
    }

    @Test
    void getAllUsersRequests_whenRequestorExist_thenReturnRequestorsRequestsCollectionTest() {
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(userId)).thenReturn(List.of(itemRequest));

        final List<ItemRequest> requests = requestService.getAllUsersRequests(userId);

        assertDoesNotThrow(() -> userService.checkUserExist(userId));
        assertNotNull(requests);
        assertEquals(1, requests.size());
        assertEquals(itemRequest, requests.get(0));

        verify(userService, times(2)).checkUserExist(userId);
        verify(requestRepository, times(1))
                .findByRequestorIdOrderByCreatedDesc(userId);
    }

    @Test
    void getAllUsersRequests_whenRequestorisNotExist_thenNotFoundExceptionThrownTest() {
        doThrow(NotFoundException.class).when(userService).checkUserExist(userId);

        assertThrows(NotFoundException.class, () -> requestService.getAllUsersRequests(userId));

        verify(userService, times(1))
                .checkUserExist(userId);
        verify(requestRepository, never())
                .findByRequestorIdOrderByCreatedDesc(userId);
    }

    @Test
    void getAll_whenInvoked_thenReturnRequestsCollectionTest() {
        final ItemRequestRequestParam params = ItemRequestRequestParam.builder()
                .from(0)
                .size(30)
                .build();
        final PageRequest pr = PageRequest.of(params.getFrom(), params.getSize());
        final Page<ItemRequest> page = new PageImpl<>(List.of(itemRequest));
        when(requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pr)).thenReturn(page);

        final List<ItemRequest> requests = requestService.getAll(userId, params);

        assertNotNull(requests);
        assertEquals(List.of(itemRequest), requests);
        verify(requestRepository, times(1))
                .findByRequestorIdNotOrderByCreatedDesc(userId, pr);
    }
}