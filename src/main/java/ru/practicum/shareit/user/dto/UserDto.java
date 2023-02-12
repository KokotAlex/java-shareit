package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Data
public class UserDto {
    private Long id;

    @NotBlank(message = "Email пользователя должен быть указан")
    @Email(message = "Неправильно задан Email пользователя")
    private String email;

    @NotBlank(message = "Имя пользователя должно быть указано")
    @Size(max = 50, message = "Имя пользователя не может быть длиннее 50 символов")
    private String name;
}
