package ru.practicum.shareit.booking.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.BookingRequestParam;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceImplTest {

    @Captor
    ArgumentCaptor<Booking> bookingCaptor;

    BookingRepository repository;
    UserService userService;
    ItemService itemService;
    BookingService bookingService;

    User booker;
    Item item;
    Booking booking;


    @BeforeEach
    void beforeEach() {
        repository = mock(BookingRepository.class);
        userService = mock(UserService.class);
        itemService = mock(ItemService.class);
        bookingService = new BookingServiceImpl(repository, userService, itemService);

        booker = User.builder()
                .id(1L)
                .email("user@email.com")
                .name("user")
                .build();
        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(User.builder().id(2L).build())
                .request(ItemRequest.builder().id(1L).build())
                .build();
        booking = Booking.builder()
                .id(1L)
                .start(now().plusDays(2))
                .end(now().plusMonths(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }


    @Test
    void getById_WhenBookingFound_thenReturnedBookingTest() {
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        final Booking foundedBooking = bookingService.getById(booking.getId());

        assertNotNull(foundedBooking);
        assertEquals(booking, foundedBooking);
        verify(repository, times(1))
                .findById(booking.getId());
    }

    @Test
    void getById_WhenBookingNotFound_thenNotFoundExceptionThrownTest() {
        when(repository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getById(booking.getId()));
        verify(repository, times(1))
                .findById(booking.getId());
    }

    @Test
    void save_whenSaveBookingWithExactingAvailableItemAndUserAndCorrectDateAndBooker_thenReturnBookingTest() {
        // Этап 1. Подготовка.
        Booking bookingToSave = Booking.builder()
                .start(now().plusDays(2))
                .end(now().plusMonths(1))
                .build();
        Booking exactingBooking = Booking.builder()
                .id(1L)
                .start(bookingToSave.getStart())
                .end(bookingToSave.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(itemService.getById(item.getId())).thenReturn(item);
        when(userService.getById(booking.getBooker().getId())).thenReturn(booking.getBooker());
        when(repository.save(any())).thenReturn(exactingBooking);

        // Этап 2. Выполнение.
        Booking returnedBooking = bookingService.save(booker.getId(), bookingToSave, item.getId());

        // Этап 3. Проверка.
        verify(repository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();

        // Проверим, что сохраняемое значение совпадает с ожидаемым.
        assertEquals(exactingBooking.getStart(), savedBooking.getStart());
        assertEquals(exactingBooking.getEnd(), savedBooking.getEnd());
        assertEquals(exactingBooking.getItem(), savedBooking.getItem());
        assertEquals(exactingBooking.getBooker(), savedBooking.getBooker());
        assertEquals(exactingBooking.getStatus(), savedBooking.getStatus());

        // Проверим, что возвращаемое значение соответствует ожидаемому.
        assertEquals(exactingBooking.getId(), returnedBooking.getId());
        assertEquals(exactingBooking.getStart(), returnedBooking.getStart());
        assertEquals(exactingBooking.getEnd(), returnedBooking.getEnd());
        assertEquals(exactingBooking.getItem(), returnedBooking.getItem());
        assertEquals(exactingBooking.getBooker(), returnedBooking.getBooker());
        assertEquals(exactingBooking.getStatus(), returnedBooking.getStatus());

        // Проверим вызовы.
        InOrder inOrder = inOrder(itemService, userService, repository);
        inOrder.verify(itemService, times(1))
                .getById(item.getId());
        inOrder.verify(userService, times(1))
                .getById(booking.getBooker().getId());
        inOrder.verify(repository, times(1))
                .save(savedBooking);
    }

    @Test
    void save_whenSaveBookingWithNotAvailableItem_thenBadRequestExceptionThrownTest() {
        item.setAvailable(false);
        when(itemService.getById(item.getId())).thenReturn(item);

        assertThrows(BadRequestException.class, () -> bookingService.save(booking.getBooker().getId(), booking, item.getId()));
        verify(itemService, times(1))
                .getById(item.getId());
        verify(repository, never())
                .save(any());
    }

    @Test
    void save_whenSaveBookingWithEndIsBeforeNow_thenBadRequestExceptionThrownTest() {
        booking.setStart(now().plusDays(2));
        booking.setEnd(now().minusSeconds(1));
        when(itemService.getById(item.getId())).thenReturn(item);

        assertThrows(BadRequestException.class, () -> bookingService.save(booking.getBooker().getId(), booking, item.getId()));
        verify(itemService, times(1))
                .getById(item.getId());
        verify(repository, never())
                .save(any());
    }

    @Test
    void save_whenSaveBookingWithStartIsBeforeNow_thenBadRequestExceptionThrownTest() {
        booking.setStart(now().minusSeconds(1));
        booking.setEnd(now().plusDays(2));
        when(itemService.getById(item.getId())).thenReturn(item);

        assertThrows(BadRequestException.class, () -> bookingService.save(booking.getBooker().getId(), booking, item.getId()));
        verify(itemService, times(1))
                .getById(item.getId());
        verify(repository, never())
                .save(any());
    }

    @Test
    void save_whenSaveBookingWithEndBeforeStart_thenBadRequestExceptionThrownTest() {
        booking.setStart(now().plusDays(3));
        booking.setEnd(now().plusDays(2));
        when(itemService.getById(item.getId())).thenReturn(item);

        assertThrows(BadRequestException.class, () -> bookingService.save(booking.getBooker().getId(), booking, item.getId()));
        verify(itemService, times(1))
                .getById(item.getId());
        verify(repository, never())
                .save(any());
    }

    @Test
    void save_whenSaveBookingWithEqualsBookerAndItemOwner_thenNotFoundExceptionThrownTest() {
        booking.getItem().setOwner(booking.getBooker());
        when(itemService.getById(item.getId())).thenReturn(item);

        assertThrows(NotFoundException.class, () -> bookingService.save(booking.getBooker().getId(), booking, item.getId()));
        verify(itemService, times(1))
                .getById(item.getId());
        verify(repository, never())
                .save(any());
    }

    @Test
    void approve_whenApproveTrue_thenReturnApprovedBookingTest() {
        // Этап 1. Подготовка.
        booking.setStatus(BookingStatus.WAITING);
        Booking exactingBooking = Booking.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(BookingStatus.APPROVED)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(repository.save(any())).thenReturn(exactingBooking);

        // Этап 2. Выполнение.
        Booking returnedBooking = bookingService.approve(booking.getItem().getOwner().getId(), booking.getId(), true);

        // Этап 3. Проверка.
        verify(repository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();

        // Проверим, что сохраняемое значение совпадает с ожидаемым.
        assertEquals(exactingBooking.getStart(), savedBooking.getStart());
        assertEquals(exactingBooking.getEnd(), savedBooking.getEnd());
        assertEquals(exactingBooking.getItem(), savedBooking.getItem());
        assertEquals(exactingBooking.getBooker(), savedBooking.getBooker());
        assertEquals(exactingBooking.getStatus(), savedBooking.getStatus());

        // Проверим, что возвращаемое значение соответствует ожидаемому.
        assertEquals(exactingBooking.getId(), returnedBooking.getId());
        assertEquals(exactingBooking.getStart(), returnedBooking.getStart());
        assertEquals(exactingBooking.getEnd(), returnedBooking.getEnd());
        assertEquals(exactingBooking.getItem(), returnedBooking.getItem());
        assertEquals(exactingBooking.getBooker(), returnedBooking.getBooker());
        assertEquals(exactingBooking.getStatus(), returnedBooking.getStatus());

        // Проверим вызовы.
        verify(repository, times(1))
                .findById(booking.getId());
        verify(repository, times(1))
                .save(savedBooking);
    }

    @Test
    void approve_whenApproveFalse_thenReturnRejectedBookingTest() {
        // Этап 1. Подготовка.
        booking.setStatus(BookingStatus.WAITING);
        Booking exactingBooking = Booking.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(BookingStatus.REJECTED)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(repository.save(any())).thenReturn(exactingBooking);

        // Этап 2. Выполнение.
        Booking returnedBooking = bookingService.approve(booking.getItem().getOwner().getId(), booking.getId(), false);

        // Этап 3. Проверка.
        verify(repository).save(bookingCaptor.capture());
        Booking savedBooking = bookingCaptor.getValue();

        // Проверим, что сохраняемое значение совпадает с ожидаемым.
        assertEquals(exactingBooking.getStart(), savedBooking.getStart());
        assertEquals(exactingBooking.getEnd(), savedBooking.getEnd());
        assertEquals(exactingBooking.getItem(), savedBooking.getItem());
        assertEquals(exactingBooking.getBooker(), savedBooking.getBooker());
        assertEquals(exactingBooking.getStatus(), savedBooking.getStatus());

        // Проверим, что возвращаемое значение соответствует ожидаемому.
        assertEquals(exactingBooking.getId(), returnedBooking.getId());
        assertEquals(exactingBooking.getStart(), returnedBooking.getStart());
        assertEquals(exactingBooking.getEnd(), returnedBooking.getEnd());
        assertEquals(exactingBooking.getItem(), returnedBooking.getItem());
        assertEquals(exactingBooking.getBooker(), returnedBooking.getBooker());
        assertEquals(exactingBooking.getStatus(), returnedBooking.getStatus());

        // Проверим вызовы.
        verify(repository, times(1))
                .findById(booking.getId());
        verify(repository, times(1))
                .save(savedBooking);
    }

    @Test
    void approve_whenEqualsBookerAndItemOwner_thenReturnNotFoundExceptionTest() {
        booking.getItem().setOwner(booking.getBooker());
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.approve(booking.getItem().getOwner().getId(), booking.getId(), true));
        verify(repository, times(1))
                .findById(booking.getId());
        verify(repository, never())
                .save(any());
    }

    @Test
    void approve_whenBookingStatusIsNotWaiting_thenReturnBadRequestExceptionTest() {
        booking.setStatus(BookingStatus.REJECTED);
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class,
                () -> bookingService.approve(booking.getItem().getOwner().getId(), booking.getId(), true));
        verify(repository, times(1))
                .findById(booking.getId());
        verify(repository, never())
                .save(any());
    }

    @Test
    void getByIdAndUserId_WhenUserIsBookerOrItemOwner_thenReturnedBookingTest() {
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        final Booking foundedBooking = bookingService.getByIdAndUserId(booking.getId(),
                booking.getBooker().getId());

        assertNotNull(foundedBooking);
        assertEquals(booking, foundedBooking);
        verify(repository, times(1))
                .findById(booking.getId());
    }

    @Test
    void getByIdAndUserId_WhenBookingNotFound_thenNotFoundExceptionThrownTest() {
        when(repository.findById(booking.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getByIdAndUserId(booking.getId(),
                booking.getBooker().getId()));
        verify(repository, times(1))
                .findById(booking.getId());
    }

    @Test
    void getByIdAndUserId_WhenUserIsNotBookerOrItemOwner_thenNotFoundExceptionThrownTest() {
        final Long anotherUserId = 10L;
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getByIdAndUserId(booking.getId(),
                anotherUserId));
        verify(repository, times(1))
                .findById(booking.getId());
    }

    @Test
    void getBookingsByBookerId_whenItIsBookersRequest_thenReturnCollectionBookersBookingsTest() {
        final BookingRequestParam params = BookingRequestParam.builder()
                .from(0)
                .size(30)
                .state("CURRENT")
                .build();
        final Page<Booking> foundBookings = new PageImpl<>(List.of(booking));
        final Sort sort = Sort.by("Start").descending();
        final MyPageRequest pageRequest = new MyPageRequest(params.getFrom(), params.getSize(), sort);

        when(repository.findAll(any(BooleanExpression.class), eq(pageRequest)))
                .thenReturn(foundBookings);

        List<Booking> returnedBookings = bookingService.getBookingsByBookerId(booking.getBooker().getId(), params);

        assertNotNull(returnedBookings);
        assertEquals(1, returnedBookings.size());
        assertEquals(booking, returnedBookings.get(0));

        verify(repository, times(1))
                .findAll(any(BooleanExpression.class), eq(pageRequest));
    }

    @Test
    void getBookingsByOwnerId_whenItIsRequestOfItemOwner_thenReturnCollectionOwnersBookingsTest() {
        final BookingRequestParam params = BookingRequestParam.builder()
                .from(0)
                .size(30)
                .state("PAST")
                .build();
        final Page<Booking> foundBookings = new PageImpl<>(List.of(booking));
        final Sort sort = Sort.by("Start").descending();
        final MyPageRequest pageRequest = new MyPageRequest(params.getFrom(), params.getSize(), sort);
        final Long ItemOwnerId = booking.getItem().getOwner().getId();
        when(repository.findAll(any(BooleanExpression.class), eq(pageRequest)))
                .thenReturn(foundBookings);

        List<Booking> returnedBookings = bookingService.getBookingsByOwnerId(ItemOwnerId, params);

        assertNotNull(returnedBookings);
        assertEquals(1, returnedBookings.size());
        assertEquals(booking, returnedBookings.get(0));

        verify(repository, times(1))
                .findAll(any(BooleanExpression.class), eq(pageRequest));
    }

    @Test
    void getBookingsByOwnerId_whenStatusIsNotCorrect_thenBadRequestExceptionThrownTest() {
        final BookingRequestParam params = BookingRequestParam.builder()
                .from(0)
                .size(30)
                .state("IncorrectStatus")
                .build();
        assertThrows(BadRequestException.class,
                () -> bookingService.getBookingsByOwnerId(booking.getItem().getId(), params));
        verify(repository, never())
                .findAll(any(BooleanExpression.class), any(MyPageRequest.class));
    }
}