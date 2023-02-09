package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class BookingDto {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private ItemDto.Nested item;
    private UserDto.Nested booker;

    @Builder
    @Getter @Setter
    @AllArgsConstructor @NoArgsConstructor
    public static class Nested {
        private Long id;
        private LocalDateTime start;
        private LocalDateTime end;
        private BookingStatus status;
        private Long itemId;
        private Long bookerId;
    }

}
