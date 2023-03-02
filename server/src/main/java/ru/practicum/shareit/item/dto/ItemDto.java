package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDto.Nested lastBooking;
    private BookingDto.Nested nextBooking;
    private Set<CommentDto.Nested> comments;
    private long requestId;

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Nested {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long lastBookingId;
        private Long nextBookingId;
        private long requestId;
    }


}
