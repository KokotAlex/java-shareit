package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    private final UserDto userDto = UserDto.builder().id(1L).name("User").email("User@mail.com").build();
    private final User user = User.builder().id(1L).name("User").email("User@mail.com").build();

    @Test
    void getAllUsers_whenInvoked_thenResponseStatusOkWithUsersCollectionInBodyTest() throws Exception {
        when(userService.getAll())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                        .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                        .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                        .andExpect(jsonPath("$[0].email", is(userDto.getEmail())))
                        .andExpect(jsonPath("$", hasSize(1)));

        verify(userService, times(1))
                .getAll();
    }

    @Test
    void saveNewUser_whenInvoked_thenResponseStatusOkWithUserDtoInBodyTest() throws Exception  {
        when(userService.save(user))
                .thenReturn(user);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1))
                .save(user);
    }

    @Test
    void updateUser_whenInvoked_thenResponseStatusOkWithUserDtoInBodyTest() throws Exception {
        Long userId = 1L;
        when(userService.update(userId, user))
                .thenReturn(user);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1))
                .update(userId, user);
    }

    @Test
    void getUserById_whenInvoked_thenResponseStatusOkWithUserDtoInBodyTest() throws Exception {
        when(userService.getById(user.getId()))
                .thenReturn(user);

        mockMvc.perform(get("/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1))
                .getById(user.getId());
    }

    @Test
    void deleteUserById_whenInvoked_thenResponseStatusOkTest() throws Exception {

        mockMvc.perform(delete("/users/{userId}", user.getId()))
                .andExpect(status().isOk());

        verify(userService, times(1))
                .deleteById(user.getId());
    }
}