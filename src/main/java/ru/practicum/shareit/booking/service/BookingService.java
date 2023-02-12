package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.Booking;

import java.util.List;

public interface BookingService {

    Booking getById(Long bookingId);

    Booking save(Long bookerId, Booking booking, Long itemId);

    Booking approve(Long ownerId, Long bookingId, Boolean approved);

    Booking getByIdAndUserId(Long bookingId, Long userId);

    List<Booking> getBookingsByBookerId(Long bookerId, String state);

    List<Booking> getBookingsByOwnerId(Long ownerId, String state);
}
