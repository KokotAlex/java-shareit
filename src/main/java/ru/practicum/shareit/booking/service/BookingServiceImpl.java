package ru.practicum.shareit.booking.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.MyPageRequest;
import ru.practicum.shareit.booking.BookingRequestParam;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        Booking foundedBooking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(Booking.class.getSimpleName(), bookingId));

        log.info("Finish getting booking by id {}", bookingId);

        return foundedBooking;
    }

    @Override
    @Transactional
    public Booking save(Long bookerId, Booking booking, Long itemId) {
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

    @Override
    @Transactional
    public Booking approve(Long ownerId, Long bookingId, Boolean approved) {
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

    @Override
    public Booking getByIdAndUserId(Long bookingId, Long userId) {
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

    @Override
    public List<Booking> getBookingsByBookerId(Long bookerId, BookingRequestParam params) {
        log.info("Start getting {} bookings by booker with id {}", params.getState(), bookerId);

        userService.checkUserExist(bookerId);

        // Для поиска запросов используем QueryDSL чтобы было удобно настраивать разные варианты фильтров
        QBooking booking = QBooking.booking;

        // Мы будем анализировать какие фильтры указал пользователь.
        // Сформируем список фильтров, соответствующих интересуемому состоянию бронирования.
        List<BooleanExpression> conditions = getStateConditions(booking, params.getState());

        // Условие, которое будет проверяться всегда - пользователь сделавший запрос
        // должен получить только свои бронирования.
        conditions.add(booking.booker.id.eq(bookerId));

        // из всех подготовленных условий, составляем единое условие
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();

        Sort sort = Sort.by("Start").descending();
        MyPageRequest pageRequest = new MyPageRequest(params.getFrom(), params.getSize(), sort);

        Iterable<Booking> foundBookings = repository.findAll(finalCondition, pageRequest);

        // Преобразуем результат поиска в список.
        List<Booking> bookings = new ArrayList<>();
        foundBookings.forEach(bookings::add);

        log.info("Finish getting {} bookings by booker with id {}", params.getState(), bookerId);

        return bookings;
    }

    @Override
    public List<Booking> getBookingsByOwnerId(Long ownerId, BookingRequestParam params) {
        log.info("Start getting {} bookings by owner with id {}", params.getState(), ownerId);

        userService.checkUserExist(ownerId);

        // Для поиска запросов используем QueryDSL чтобы было удобно настраивать разные варианты фильтров
        QBooking booking = QBooking.booking;

        // Мы будем анализировать какие фильтры указал пользователь.
        // Сформируем список фильтров, соответствующих интересуемому состоянию бронирования.
        List<BooleanExpression> conditions = getStateConditions(booking, params.getState());

        // Условие, которое будет проверяться всегда - необходимо получить бронирования
        // вещей, принадлежащих указанному пользователю.
        conditions.add(booking.item.owner.id.eq(ownerId));

        // из всех подготовленных условий, составляем единое условие
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();

        Sort sort = Sort.by("Start").descending();
        MyPageRequest pageRequest = new MyPageRequest(params.getFrom(), params.getSize(), sort);

        Iterable<Booking> foundBookings = repository.findAll(finalCondition, pageRequest);

        // Преобразуем результат поиска в список.
        List<Booking> bookings = new ArrayList<>();
        foundBookings.forEach(bookings::add);

        log.info("Finish getting {} bookings by owner with id {}", params.getState(), ownerId);

        return bookings;
    }

    private static List<BooleanExpression> getStateConditions(QBooking booking, String state) {
        List<BooleanExpression> conditions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        switch (state.toUpperCase()) {
            case "ALL":
                break;
            case "CURRENT":
                // Получим текущие бронирования.
                conditions.add(booking.start.before(now));
                conditions.add(booking.end.after(now));
                break;
            case "PAST":
                // Получим завершенные бронирования.
                conditions.add(booking.end.before(now));
                break;
            case "FUTURE":
                // Получим будущие бронирования.
                conditions.add(booking.start.after(now));
                break;
            case "WAITING":
            case "REJECTED":
                // Получим завершенные или ожидающие подтверждения бронирования.
                conditions.add(booking.status.eq(BookingStatus.valueOf(state)));
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        return conditions;
    }

}
