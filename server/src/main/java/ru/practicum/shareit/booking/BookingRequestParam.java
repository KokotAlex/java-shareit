package ru.practicum.shareit.booking;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BookingRequestParam {
    private Integer from;
    private Integer size;
    private String state;
}
