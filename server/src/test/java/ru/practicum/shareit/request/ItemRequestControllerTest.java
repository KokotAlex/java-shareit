package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static java.time.LocalDateTime.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    RequestService requestService;

    @Autowired
    MockMvc mockMvc;

    final String headerUserId = "X-Sharer-User-Id";
    final LocalDateTime time = of(2020, 1, 1, 0, 0, 1);
    final Long userId = 1L;
    final UserDto.Nested requestorDtoNested = UserDto.Nested.builder()
            .id(userId)
            .email("requestor@email.com")
            .name("requestor")
            .build();
    final User requestor = User.builder()
            .id(userId)
            .email("requestor@email.com")
            .name("requestor")
            .build();
    final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("ItemDescription")
            .created(time)
            .requestor(requestorDtoNested)
            .build();
    final ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .description("ItemDescription")
            .created(time)
            .requestor(requestor)
            .build();
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    void saveNewRequest_whenInvoked_thenResponseStatusOkWithRequestDtoInBodyTest() throws Exception  {
        when(requestService.save(userId, itemRequest))
                .thenReturn(itemRequest);

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header(headerUserId, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated()
                        .format(dateTimeFormatter))));

        verify(requestService, times(1))
                .save(userId, itemRequest);
    }

    @Test
    void getAllUsersRequests_whenUserIsRequestor_thenResponseStatusOkWithRequestsCollectionInBodyTest() throws Exception {
        when(requestService.getAllUsersRequests(userId))
                .thenReturn(List.of(itemRequest));

        mockMvc.perform(get("/requests")
                        .header(headerUserId, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDto.getCreated()
                        .format(dateTimeFormatter))))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(requestService, times(1))
                .getAllUsersRequests(userId);
    }

    @Test
    void getAllUsersRequests_whenUserIsNotRequestor_thenResponseStatusOkWithEmptyCollectionInBodyTest() throws Exception {
        Long anotherUserId = 2L;

        when(requestService.getAllUsersRequests(anotherUserId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header(headerUserId, anotherUserId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(requestService, times(1))
                .getAllUsersRequests(anotherUserId);
    }

    @Test
    void getRequestById_whenInvoked_thenResponseStatusOkWithRequestDtoInBodyTest() throws Exception {
        when(requestService.getById(itemRequest.getId(), userId))
                .thenReturn(itemRequest);

        mockMvc.perform(get("/requests/{requestId}", itemRequest.getId())
                        .header(headerUserId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated()
                        .format(dateTimeFormatter))));

        verify(requestService, times(1))
                .getById(itemRequest.getId(), userId);
    }

    @Test
    void getAll_whenInvoked_thenResponseStatusOkWithRequestCollectionInBodyTest() throws Exception {
        final ItemRequestRequestParam defaultParams = ItemRequestRequestParam.builder()
                .from(0)
                .size(30)
                .build();
        when(requestService.getAll(eq(userId), eq(defaultParams)))
                .thenReturn(List.of(itemRequest));

        mockMvc.perform(get("/requests/all")
                        .header(headerUserId, userId)
                        .param("from", defaultParams.getFrom().toString())
                        .param("size", defaultParams.getSize().toString())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestDto.getCreated()
                        .format(dateTimeFormatter))))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(requestService, times(1))
                .getAll(eq(userId), eq(defaultParams));
    }
}