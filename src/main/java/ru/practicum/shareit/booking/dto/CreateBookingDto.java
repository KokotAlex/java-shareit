package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CreateBookingDto {

    private Long id;

    @NotNull(message = "Идентификатор заказываемой вещи должен быть указан.")
    @Min(value = 1, message = "Идентификатор заказываемой вещи должен быть положительным.")
    private Long itemId;

    @NotNull(message = "Дата начала бронирования должна быть заполнена")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования должна быть заполнена")
    private LocalDateTime end;

    private BookingStatus status;

}
