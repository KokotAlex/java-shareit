package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    @MockBean
    ItemService itemService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    final String headerUserId = "X-Sharer-User-Id";
    final Long userId = 1L;
    final Long requestId = 1L;
    final User owner = User.builder()
            .id(userId)
            .email("owner@email.com")
            .name("owner")
            .build();

    final Item item = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .owner(owner)
            .request(ItemRequest.builder().id(requestId).build())
            .build();

    final Booking lastBooking = Booking.builder()
            .id(1L)
            .item(item)
            .booker(item.getOwner())
            .start(now().minusMonths(2))
            .end(now().plusDays(1))
            .status(BookingStatus.CANCELED)
            .build();
    final BookingDto.Nested lastBookingDtoNested = BookingDto.Nested.builder()
            .id(lastBooking.getId())
            .itemId(lastBooking.getItem().getId())
            .bookerId(lastBooking.getBooker().getId())
            .start(now().minusMonths(2))
            .end(now().plusDays(1))
            .status(BookingStatus.CANCELED)
            .build();

    final Booking nextBooking = Booking.builder()
            .id(2L)
            .item(item)
            .booker(item.getOwner())
            .start(now().plusDays(2))
            .end(now().plusMonths(1))
            .status(BookingStatus.APPROVED)
            .build();

    final BookingDto.Nested nextBookingDtoNested = BookingDto.Nested.builder()
            .id(nextBooking.getId())
            .itemId(nextBooking.getItem().getId())
            .bookerId(nextBooking.getBooker().getId())
            .start(nextBooking.getStart())
            .end(nextBooking.getEnd())
            .status(nextBooking.getStatus())
            .build();

    final Comment comment = Comment.builder()
            .id(1L)
            .text("CommentsText")
            .author(owner)
            .created(now().minusDays(1))
            .build();
    final CommentDto commentDto = CommentDto.builder()
            .id(comment.getId())
            .text(comment.getText())
            .authorName(comment.getAuthor().getName())
            .created(comment.getCreated())
            .build();
    final CommentDto.Nested commentDtoNested = CommentDto.Nested.builder()
            .id(comment.getId())
            .text(comment.getText())
            .authorName(comment.getAuthor().getName())
            .created(comment.getCreated())
            .build();

    final ItemDto itemDto = ItemDto.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .available(item.getAvailable())
            .requestId(item.getRequest().getId())
            .lastBooking(lastBookingDtoNested)
            .nextBooking(nextBookingDtoNested)
            .comments(new HashSet<>(List.of(commentDtoNested)))
            .build();

    @Test
    void saveNewItem_whenInvoked_thenResponseStatusOkWithItemDtoInBodyTest() throws Exception  {
        item.getBookings().add(lastBooking);
        item.getBookings().add(nextBooking);
        when(itemService.save(userId, item, itemDto.getRequestId()))
                .thenReturn(item);

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header(headerUserId, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));

        verify(itemService, times(1))
                .save(userId, item, itemDto.getRequestId());
    }

    @Test
    void update_whenInvoked_thenResponseStatusOkWithItemDtoInBodyTest() throws Exception {
        when(itemService.update(userId, item.getId(), itemDto.getRequestId(), item))
                .thenReturn(item);

        mockMvc.perform(patch("/items/{itemId}", item.getId())
                        .header(headerUserId, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));

        verify(itemService, times(1))
                .update(userId, item.getId(), itemDto.getRequestId(), item);
    }

    @Test
    void getById_whenInvoked_thenResponseStatusOkWithItemDtoInBodyTest() throws Exception {
        when(itemService.getById(item.getId()))
                .thenReturn(item);

        mockMvc.perform(get("/items/{itemId}", item.getId())
                        .header(headerUserId, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));

        verify(itemService, times(1))
                .getById(item.getId());
    }

    @Test
    void getAllOwnersItems_whenUserIsOwner_thenResponseStatusOkWithItemDtoCollectionInBodyTest() throws Exception {
        when(itemService.getAll(userId))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/items")
                        .header(headerUserId, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(itemDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(itemService, times(1))
                .getAll(userId);
    }

    @Test
    void findItemsByText_whenItemContainText_thenResponseStatusOkWithItemDtoCollectionInBodyTest() throws Exception {
        when(itemService.findByText(item.getName()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .header(headerUserId, userId)
                        .param("text", item.getName())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(itemDto.getRequestId()), Long.class))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(itemService, times(1))
                .findByText(item.getName());
    }

    @Test
    void saveNewComment_whenInvoked_thenResponseStatusOkWithCommentDtoInBodyTest() throws Exception  {
        when(itemService.saveComment(comment, userId, item.getId()))
                .thenReturn(comment);

        mockMvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header(headerUserId, userId)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));

        verify(itemService, times(1))
                .saveComment(comment, userId, item.getId());
    }
}