package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    UserService userService;
    UserRepository userRepository;
    User user1;

    @Captor
    ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);
        user1 = User.builder().id(1L).name("User1").email("User1@email").build();
    }

    @Test
    void getAll_whenInvoked_thenReturnUsersCollectionTest() {
        when(userRepository.findAll()).thenReturn(List.of(user1));

        final List<User> users = userService.getAll();

        assertNotNull(users);
        assertEquals(List.of(user1), users);
        verify(userRepository, times(1))
                .findAll();
    }

    @Test
    void save_whenSaveUser_thenReturnUserTest() {
        when(userRepository.save(user1)).thenReturn(user1);

        final User savedUsers = userService.save(user1);

        assertEquals(user1, savedUsers);
        verify(userRepository, times(1))
                .save(user1);
    }

    @Test
    void getById_WhenUserFound_thenReturnedUserTest() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        final User gettingUser = userService.getById(user1.getId());

        assertNotNull(gettingUser);
        assertEquals(user1, gettingUser);
        verify(userRepository, times(1))
                .findById(user1.getId());
    }

    @Test
    void getById_WhenUserNotFound_thenNotFoundExceptionThrownTest() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(user1.getId()));
        verify(userRepository, times(1))
                .findById(user1.getId());
    }

    @Test
    void update_WhenEmailAndNameExist_ThenUserReturnWithUpdatedEmailAndNameTest() {
        User userForUpdate = User.builder().name("UpdatedUser1").email("UpdatedUser1@email").build();
        User expectedUser = User.builder().id(user1.getId()).name("UpdatedUser1").email("UpdatedUser1@email").build();

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any())).thenReturn(expectedUser);

        User returnedUser = userService.update(user1.getId(), userForUpdate);

        verify(userRepository).save(userArgumentCaptor.capture());
        User userForSave = userArgumentCaptor.getValue();

        // Проверим, что пользователь для сохранения в БД равен возвращенному из метода.
        assertEquals(returnedUser.getId(), userForSave.getId());
        assertEquals(returnedUser.getName(), userForSave.getName());
        assertEquals(returnedUser.getEmail(), userForSave.getEmail());

        // Проверим, что поля возвращенного пользователя соответствуют ожидаемым.
        assertEquals(expectedUser.getId(), returnedUser.getId());
        assertEquals(expectedUser.getName(), returnedUser.getName());
        assertEquals(expectedUser.getEmail(), returnedUser.getEmail());

        // Проверим наличие вызовов в БД.
        verify(userRepository, times(1))
                .findById(user1.getId());
        verify(userRepository, times(1))
                .save(expectedUser);
    }

    @Test
    void update_WhenEmailAndNameNotExist_ThenUserReturnWithoutUpdatedEmailAndNameTest() {
        User userForUpdate = new User();

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(any())).thenReturn(user1);

        User returnedUser = userService.update(user1.getId(), userForUpdate);

        verify(userRepository).save(userArgumentCaptor.capture());
        User userForSave = userArgumentCaptor.getValue();

        // Проверим, что пользователь для сохранения в БД равен возвращенному из метода.
        assertEquals(returnedUser.getId(), userForSave.getId());
        assertEquals(returnedUser.getName(), userForSave.getName());
        assertEquals(returnedUser.getEmail(), userForSave.getEmail());

        // Проверим, что поля возвращенного пользователя соответствуют ожидаемым.
        assertEquals(user1.getId(), returnedUser.getId());
        assertEquals(user1.getName(), returnedUser.getName());
        assertEquals(user1.getEmail(), returnedUser.getEmail());

        // Проверим наличие вызовов в БД.
        verify(userRepository, times(1))
                .findById(user1.getId());
        verify(userRepository, times(1))
                .save(user1);
    }

    @Test
    void deleteById_whenInvoked_thenDeletionMethodCalledOnceTest() {
        userService.deleteById(user1.getId());

        verify(userRepository, times(1))
                .deleteById((user1.getId()));
    }

    @Test
    void checkUserExist_userExist_thenNotThrown() {
        when(userRepository.existsById(user1.getId())).thenReturn(true);

        userService.checkUserExist(user1.getId());

        verify(userRepository, times(1)).existsById(user1.getId());
    }

    @Test
    void checkUserExist_userIsNotExist_thenThrown() {
        when(userRepository.existsById(user1.getId())).thenReturn(false);

        assertThrows(NotFoundException.class, () ->  userService.checkUserExist(user1.getId()));

        verify(userRepository, times(1)).existsById(user1.getId());
    }

}