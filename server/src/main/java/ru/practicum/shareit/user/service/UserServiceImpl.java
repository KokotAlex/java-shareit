package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    @Transactional
    public User save(User user) {
        log.info("Start saving user {}", user);

        User savedUser = repository.save(user);

        log.info("Finish saving user {}", savedUser);

        return savedUser;
    }

    @Override
    public User getById(Long userId) {
        log.info("Start getting user by id {}", userId);

        User gettingUser = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(User.class.getSimpleName(), userId));

        log.info("Finish getting user by id {}", userId);

        return gettingUser;
    }

    @Override
    @Transactional
    public User update(Long userId, User user) {
        log.info("Start updating user by id {}", userId);

        User userForUpdate = getById(userId);

        // Обновим Email
        String userEmail = user.getEmail();
        if (userEmail != null) {
            userForUpdate.setEmail(userEmail);
        }
        // Обновим имя
        String userName = user.getName();
        if (userName != null) {
            userForUpdate.setName(userName);
        }

        User updatedUser = repository.save(userForUpdate);

        log.info("Finish updating user by id {}", userId);

        return updatedUser;
    }

    @Override
    @Transactional
    public void deleteById(Long userId) {
        log.info("Start deletion user by id {}", userId);

        repository.deleteById(userId);

        log.info("Finish deletion user by id {}", userId);
    }

    @Override
    public void checkUserExist(Long userId) {
        if (!repository.existsById(userId)) {
            throw new NotFoundException(User.class.getSimpleName(), userId);
        }
    }
}