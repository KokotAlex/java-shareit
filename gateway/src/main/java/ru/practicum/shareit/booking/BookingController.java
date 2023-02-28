package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private static final String HEADER_USER_ID = "X-Sharer-User-Id";
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> saveNewBooking(@RequestHeader(HEADER_USER_ID) long userId,
												 @RequestBody @Valid CreateBookingDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);

		if (requestDto.getEnd().isBefore(requestDto.getStart())) {
			throw new BadRequestException("The booking start date "
					+ requestDto.getStart()
					+ " cannot be earlier than the booking end date "
					+ requestDto.getEnd());
		}

		return bookingClient.saveNewBooking(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@RequestHeader(HEADER_USER_ID) Long userId,
										  @PathVariable Long bookingId,
										  @RequestParam(defaultValue = "false") boolean approved) {
		log.info("Patch a booking {}, userId={}", bookingId, userId);

		return bookingClient.approve(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(HEADER_USER_ID) long userId,
											 @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);

		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getAllByBooker(@RequestHeader(HEADER_USER_ID) long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "30") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);

		return bookingClient.getAllByBooker(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllByOwner(@RequestHeader(HEADER_USER_ID) Long userId,
										  		@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
												@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
												@Positive @RequestParam(defaultValue = "30") Integer size) {
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);

		BookingState state = BookingState.from(stateParam)
										 .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

		return bookingClient.getAllByOwner(userId, state, from, size);
	}
}
