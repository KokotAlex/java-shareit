package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    private static final Map<Long, User> users = new HashMap<>();

    private static Long autoincrement = 0L;

    @Override
    public List<User> findAll() {
        List<User> userList = new ArrayList<>(users.values());

        log.debug("Finding all users");

        return userList;
    }

    @Override
    public Long save(User user) {
        user.setId(++autoincrement);
        Long userId = user.getId();
        users.put(userId, user);

        log.debug("Saving user with id {}",  userId);

        return userId;
    }

    @Override
    public Optional<User> findById(Long userId) {

        Optional<User> optionalUser;

        if (users.containsKey(userId)) {
            optionalUser = Optional.of(users.get(userId));
        } else {
            optionalUser = Optional.empty();
        }

        log.debug("Result finding user by id {}: {}",  userId, optionalUser);

        return optionalUser;
    }

    @Override
    public boolean emailIsExist(String email) {
        boolean result = findAll()
                .stream()
                .anyMatch(user -> user.getEmail()
                        .toLowerCase()
                        .contains(email.toLowerCase()));

        log.debug("User with email {} is exist: {}", email, result);

        return result;
    }

    @Override
    public boolean isNotExist(Long userId) {
        boolean result = !users.containsKey(userId);

        log.debug("User with id {} is exist: {}", userId, result);

        return result;
    }

    @Override
    public void update(Long userId, User user) {
        User updatedUser = users.get(userId);

        String userName = user.getName();
        String userEmail = user.getEmail();

        if (userName != null) {
            updatedUser.setName(userName);
        }

        if (userEmail != null) {
            updatedUser.setEmail(userEmail);
        }

        log.debug("Updated user with id {}", userId);
    }

    @Override
    public void removeById(Long userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);

            log.debug("removing user with id {}", userId);
        }
    }

}
