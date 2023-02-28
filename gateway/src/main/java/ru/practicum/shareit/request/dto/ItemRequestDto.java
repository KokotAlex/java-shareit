package ru.practicum.shareit.request.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

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

    private LocalDateTime created;

}
