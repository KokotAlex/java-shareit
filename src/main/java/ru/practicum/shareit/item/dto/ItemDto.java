package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Builder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Наименование вещи должно быть указано")
    @Size(max = 100, message = "Наименование вещи не может быть длиннее 100 символов")
    private String name;

    @NotBlank(message = "Описание вещи не должно быть пустым")
    @Size(max = 1000, message = "Описание вещи не может быть длиннее 1000 символов")
    private String description;

    @NotNull(message = "Необходимо указать статус доступности вещи")
    private Boolean available;

    private BookingDto.Nested lastBooking;
    private BookingDto.Nested nextBooking;
    private Set<CommentDto.Nested> comments;

    @Builder
    @Getter @Setter
    @AllArgsConstructor @NoArgsConstructor
    public static class Nested {
        private Long id;
        private String name;
        private String description;
        private Boolean available;
        private Long lastBookingId;
        private Long nextBookingId;
    }


}
