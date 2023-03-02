package ru.practicum.shareit.booking.dto;

import lombok.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookingDto {

    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;

}
