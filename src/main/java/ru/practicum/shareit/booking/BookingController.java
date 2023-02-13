package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String HEADER_AUTHOR_ID = "X-Sharer-User-Id";

    public final BookingService service;

    @PostMapping
    public BookingDto saveNewBooking(@RequestHeader(HEADER_AUTHOR_ID) Long bookerId,
                                     @Valid @RequestBody CreateBookingDto createBookingDto) {
        log.info("Handling a request to create a new booking for user with id {}", bookerId);

        Booking booking = BookingMapper.toBooking(createBookingDto);
        Booking savedBooking = service.save(bookerId, booking, createBookingDto.getItemId());

        return BookingMapper.toBookingDto(savedBooking, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(HEADER_AUTHOR_ID) Long ownerId,
                                @PathVariable Long bookingId,
                                @RequestParam(defaultValue = "false") Boolean approved) {
        log.info("Processing a booking confirmation request that has an id {}", bookingId);

        Booking booking = service.approve(ownerId, bookingId, approved);

        return BookingMapper.toBookingDto(booking, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(HEADER_AUTHOR_ID) Long userId,
                                 @PathVariable Long bookingId) {
        log.info("Processing a getting booking by user id {}", bookingId);

        Booking booking = service.getByIdAndUserId(bookingId, userId);

        return BookingMapper.toBookingDto(booking, userId);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(@RequestHeader(HEADER_AUTHOR_ID) Long bookerId,
                                   @RequestParam(defaultValue = "ALL") String state) {
        log.info("Processing a getting {} bookings for booker id {}", state, bookerId);

        List<Booking> bookings = service.getBookingsByBookerId(bookerId, state);

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, bookerId))
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(HEADER_AUTHOR_ID) Long ownerId,
                                   @RequestParam(defaultValue = "ALL") String state) {
        log.info("Processing a getting {} bookings for owner id {}", state, ownerId);

        List<Booking> bookings = service.getBookingsByOwnerId(ownerId, state);

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, ownerId))
                .collect(Collectors.toList());
    }
}
