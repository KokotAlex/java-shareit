package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAll() {
        log.info("Start getting all users");

        List<User> users = repository.findAll();

        log.info("Finish getting all users");

        return users;
    }

    @Override
    public User save(User user) {
        log.info("Start saving user {}", user);

        checkUsersEmail(user.getEmail());

        Long userId = repository.save(user);
        User newUser = getById(userId);

        log.info("Finish saving user {}", user);

        return newUser;
    }

    @Override
    public User getById(Long userId) {
        log.info("Start getting user by id {}", userId);

        User newUser = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(User.class.getSimpleName(), userId));

        log.info("Finish getting user by id {}", userId);

        return newUser;
    }

    @Override
    public User update(Long userId, User user) {
        log.info("Start updating user by id {}", userId);

        checkUserExist(userId);

        String userEmail = user.getEmail();
        if (userEmail != null) {
            checkUsersEmail(userEmail);
        }

        repository.update(userId, user);
        User updatedUser = getById(userId);

        log.info("Finish updating user by id {}", userId);

        return updatedUser;
    }

    @Override
    public void deleteById(Long userId) {
        log.info("Start deletion user by id {}", userId);

        repository.removeById(userId);

        log.info("Finish deletion user by id {}", userId);
    }

    @Override
    public void checkUserExist(Long userId) {
        if (repository.isNotExist(userId)) {
            throw new NotFoundException(User.class.getSimpleName(), userId);
        }
    }

    private void checkUsersEmail(String Email) {
        if (repository.emailIsExist(Email)) {
            throw new BadRequestException("Пользователь с Email " + Email + " уже существует.");
        }
    }
}