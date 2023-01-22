package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Handling get all users request");

        return service.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public UserDto saveNewUser(@Valid @RequestBody UserDto userDto) {
        log.info("Handling a request to create a new user");

        User user = UserMapper.toUser(userDto);
        User createdUser = service.save(user);

        return UserMapper.toUserDto(createdUser);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Handling a request to update the user with id {}", userId);

        User user = UserMapper.toUser(userDto);
        User updatedUser = service.update(userId, user);

        return UserMapper.toUserDto(updatedUser);
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Handling a request to get a user with id {}", userId);

        User user = service.getById(userId);

        return UserMapper.toUserDto(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("Handling a request to delete a user with id {}", userId);

        service.deleteById(userId);
    }
}