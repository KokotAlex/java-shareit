package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    Long save(User user);

    Optional<User> findById(Long userId);

    boolean emailIsExist(String email);

    boolean isNotExist(Long userId);

    void update(Long userId, User user);

    void removeById(Long userId);
}