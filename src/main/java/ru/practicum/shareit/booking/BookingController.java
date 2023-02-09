package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

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

        return service.save(bookerId, createBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(HEADER_AUTHOR_ID) Long ownerId,
                                @PathVariable Long bookingId,
                                @RequestParam(defaultValue = "false") Boolean approved) {
        log.info("Processing a booking confirmation request that has an id {}", bookingId);

        return service.approveDto(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(HEADER_AUTHOR_ID) Long userId,
                                 @PathVariable Long bookingId) {
        log.info("Processing a getting booking by user id {}", bookingId);

        return service.getDtoByIdAndUserId(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(@RequestHeader(HEADER_AUTHOR_ID) Long bookerId,
                                   @RequestParam(defaultValue = "ALL") String state) {
        log.info("Processing a getting {} bookings for booker id {}", state, bookerId);

        return service.getBookingsDtoByBookerId(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(HEADER_AUTHOR_ID) Long ownerId,
                                   @RequestParam(defaultValue = "ALL") String state) {
        log.info("Processing a getting {} bookings for owner id {}", state, ownerId);

        return service.getBookingsDtoByOwnerId(ownerId, state);
    }
}
