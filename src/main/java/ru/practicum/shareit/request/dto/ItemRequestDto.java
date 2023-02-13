package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание запроса должно быть заполнено")
    @Size(max = 1000, message = "Описание запроса не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Автор запроса должен быть заполнен")
    private UserDto.Nested requestor;

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Nested {
        private Long id;
        private String description;
        private Long requestorId;
    }
}
