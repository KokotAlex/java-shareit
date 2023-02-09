package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

public interface BookingService {

    Booking getById(Long bookingId);

    BookingDto save(Long bookerId, CreateBookingDto createBookingDto);

    BookingDto approveDto(Long ownerId, Long bookingId, Boolean approved);

    BookingDto getDtoByIdAndUserId(Long bookingId, Long userId);

    List<BookingDto> getBookingsDtoByBookerId(Long bookerId, String state);

    List<BookingDto> getBookingsDtoByOwnerId(Long ownerId, String state);
}
