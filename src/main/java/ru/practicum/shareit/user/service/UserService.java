package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<User> getAll();

    User save(User user);

    User getById(Long userId);

    User update(Long userId, User user);

    void deleteById(Long userId);

    void checkUserExist(Long userId);
}