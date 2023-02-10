package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "Email пользователя должен быть указан")
    @Email(message = "Неправильно задан Email пользователя")
    private String email;

    @NotBlank(message = "Имя пользователя должно быть указано")
    @Size(max = 50, message = "Имя пользователя не может быть длиннее 50 символов")
    private String name;

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Nested {
        private Long id;
        private String email;
        private String name;
    }
}
