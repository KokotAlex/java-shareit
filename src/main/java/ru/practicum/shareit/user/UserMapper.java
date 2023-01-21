package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;

public class UserMapper {

    public static UserDto toUserDto(User user) {
        return UserDto.builder().
                id(user.getId()).
                email(user.getEmail()).
                name(user.getName()).
                build();
    }

    public static User toUser(UserDto user) {
        return User.builder().
                id(user.getId()).
                email(user.getEmail()).
                name(user.getName()).
                build();
    }

}
