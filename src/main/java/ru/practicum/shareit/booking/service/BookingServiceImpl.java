package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    public final BookingRepository repository;
    public final UserService userService;
    public final ItemService itemService;

    @Override
    public Booking getById(Long bookingId) {
        log.info("Start getting booking by id {}", bookingId);

        Booking foundedItem = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(Booking.class.getSimpleName(), bookingId));

        log.info("Finish getting booking by id {}", bookingId);

        return foundedItem;
    }

    @Override
    @Transactional
    public BookingDto save(Long bookerId, CreateBookingDto createBookingDto) {
        Booking booking = BookingMapper.toBooking(createBookingDto);
        Booking savedBooking = save(bookerId, booking, createBookingDto.getItemId());

        return BookingMapper.toBookingDto(savedBooking, bookerId);
    }

    @Override
    @Transactional
    public BookingDto approveDto(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = approve(ownerId, bookingId, approved);

        return BookingMapper.toBookingDto(booking, ownerId);
    }

    @Override
    public BookingDto getDtoByIdAndUserId(Long bookingId, Long userId) {
        Booking booking = getByIdAndUserId(bookingId, userId);

        return BookingMapper.toBookingDto(booking, userId);
    }

    @Override
    public List<BookingDto> getBookingsDtoByBookerId(Long bookerId, String state) {
        List<Booking> bookings = getBookingsByBookerId(bookerId, state);

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, bookerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsDtoByOwnerId(Long ownerId, String state) {
        List<Booking> bookings = getBookingsByOwnerId(ownerId, state);

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, ownerId))
                .collect(Collectors.toList());
    }

    private Booking save(Long bookerId, Booking booking, Long itemId) {
        log.info("Start saving booking {}", booking);

        Item item = itemService.getById(itemId);

        // Выполним проверку доступности товара
        if (!item.getAvailable()) {
            throw new BadRequestException("Item with id " + itemId + " is not available");
        }

        // Выполним проверки на корректность дат начала и окончания бронирования.
        LocalDateTime now = LocalDateTime.now();
        if (booking.getEnd().isBefore(now)) {
            throw new BadRequestException("The booking end date "
                    + booking.getEnd()
                    + " cannot be in the past.");
        }

        if (booking.getStart().isBefore(now)) {
            throw new BadRequestException("The booking start date "
                    + booking.getStart()
                    + " cannot be in the past.");
        }

        if (booking.getEnd().isBefore(booking.getStart())) {
            throw new BadRequestException("The booking start date "
                    + booking.getStart()
                    + " cannot be earlier than the booking end date "
                    + booking.getEnd());
        }

        // Выполним проверку на корректность заказчика.
        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Item with id " + itemId + " cannot be booked by his owner");
        }

        User booker = userService.getById(bookerId);

        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        Booking newBooking = repository.save(booking);

        log.info("Finish saving booking {}", booking);

        return newBooking;
    }

    private Booking approve(Long ownerId, Long bookingId, Boolean approved) {
        log.info("Start approving a booking with id {}", bookingId);

        // Получим заявку на бронирование.
        Booking booking = getById(bookingId);

        // Проверим, корректность подтверждения бронирования.
        if (booking.getBooker().getId().equals(ownerId)) {
            throw new NotFoundException("booker can't confirm the booking");
        }

        if (!booking.getItem().getOwner().getId().equals(ownerId)
                || !booking.getStatus().equals(BookingStatus.WAITING)) {

            throw new BadRequestException("Only the owner of the item can confirm the booking");
        }

        // Установим статус бронирования.
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        // Сохраним результат подтверждения.
        Booking savedBooking = repository.save(booking);

        log.info("Finish approving a booking with id {}", bookingId);

        return savedBooking;
    }

    private Booking getByIdAndUserId(Long bookingId, Long userId) {
        log.info("Start getting booking by id and user id {}", bookingId);

        Booking booking = getById(bookingId);

        // Проверим, что получение заказа осуществляется владельцем вещи или заказчиком.
        if (!booking.getItem().getOwner().getId().equals(userId)
                && !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Only the owner or booker can getting a booking");
        }

        log.info("Finish getting booking by id and user id {}", bookingId);

        return booking;
    }

    private List<Booking> getBookingsByBookerId(Long bookerId, String state) {
        log.info("Start getting {} bookings by booker with id {}", state, bookerId);

        userService.checkUserExist(bookerId);

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = repository.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, now, now);
                break;
            case "PAST":
                bookings = repository.findByBooker_IdAndEndBeforeOrderByStartDesc(bookerId, now);
                break;
            case "FUTURE":
                bookings = repository.findByBooker_IdAndStartAfterOrderByStartDesc(bookerId, now);
                break;
            case "WAITING":
            case "REJECTED":
                bookings = repository.findByBooker_IdAndStatusOrderByStartDesc(bookerId, BookingStatus.valueOf(state));
                break;
            case "ALL":
                bookings = repository.findByBooker_IdOrderByStartDesc(bookerId);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        log.info("Finish getting {} bookings by booker with id {}", state, bookerId);

        return bookings;
    }

    private List<Booking> getBookingsByOwnerId(Long ownerId, String state) {
        log.info("Start getting {} bookings by owner with id {}", state, ownerId);

        userService.checkUserExist(ownerId);

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        switch (state.toUpperCase()) {
            case "CURRENT":
                bookings = repository.findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now);
                break;
            case "PAST":
                bookings = repository.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(ownerId, now);
                break;
            case "FUTURE":
                bookings = repository.findByItem_Owner_IdAndStartAfterOrderByStartDesc(ownerId, now);
                break;
            case "WAITING":
            case "REJECTED":
                bookings = repository.findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.valueOf(state));
                break;
            case "ALL":
                bookings = repository.findByItem_Owner_IdOrderByStartDesc(ownerId);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        log.info("Finish getting {} bookings by owner with id {}", state, ownerId);

        return bookings;
    }
}
