package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class UserServiceImplTest {

    UserService userService;
    UserService mockUserService;
    UserRepository userRepository;
    private User user1;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
//        userService = new UserServiceImpl(userRepository);
        userService = spy(new UserServiceImpl(userRepository));
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

//        when(userRepository.save(userForUpdate)).thenReturn(userForUpdate);
        doReturn(userForUpdate).when(userRepository).save(userForUpdate);
        //when(userRepository.save(eq(userForUpdate))).thenReturn(userForUpdate);
        doReturn(user1).when(userService).getById(user1.getId());
//        when(userService.getById(user1.getId())).thenReturn(user1);
//        when(getById(any());

        User updatedUser = userService.update(user1.getId(), userForUpdate);

        assertEquals(expectedUser, updatedUser);
        verify(userRepository, times(1))
                .save(userForUpdate);
        verify(userService, times(1))
                .getById(user1.getId());
//        User userForUpdate = User.builder().name("UpdatedUser1").email("UpdatedUser1@email").build();
//        User expectedUser = User.builder().id(user1.getId()).name("UpdatedUser1").email("UpdatedUser1@email").build();
//
//        when(userRepository.save(eq(userForUpdate))).thenReturn(userForUpdate);
//        doReturn(user1).when(userService).getById(user1.getId());
//
//        User updatedUser = userService.update(user1.getId(), userForUpdate);
//
//        assertEquals(expectedUser, updatedUser);
//        verify(userRepository, times(1))
//                .save(eq(userForUpdate));
//        verify(userService, times(1))
//                .getById(user1.getId());
    }

    @Test
    void deleteById() {
    }

    @Test
    void checkUserExist() {
    }
}