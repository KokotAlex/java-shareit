package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<UserDto> getAllDto();

    UserDto save(UserDto userDto);

    User getById(Long userId);

    UserDto getDtoById(Long userId);

    UserDto update(Long userId, UserDto userDto);

    void deleteById(Long userId);

    void checkUserExist(Long userId);
}