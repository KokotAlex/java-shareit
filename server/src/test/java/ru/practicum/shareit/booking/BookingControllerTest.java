package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.LocalDateTime.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    @MockBean
    BookingService service;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    MockMvc mockMvc;

    final String headerUserId = "X-Sharer-User-Id";
    final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    final User booker = User.builder()
            .id(1L)
            .email("user@email.com")
            .name("user")
            .build();
    final UserDto.Nested bookerDtoNested = UserDto.Nested.builder()
            .id(booker.getId())
            .email(booker.getEmail())
            .name(booker.getName())
            .build();
    final Item item = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .owner(User.builder().id(2L).build())
            .request(ItemRequest.builder().id(1L).build())
            .build();
    final Booking booking = Booking.builder()
            .id(1L)
            .start(of(2022, 1, 1, 0, 0, 0))
            .end(of(2023, 1, 1, 0, 0, 0))
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();
    final CreateBookingDto createBookingDto = CreateBookingDto.builder()
            .itemId(booking.getItem().getId())
            .start(booking.getStart())
            .end(booking.getEnd())
            .build();
    final BookingDto bookingDto = BookingDto.builder()
            .id(booking.getId())
            .start(booking.getStart())
            .end(booking.getEnd())
            .status(BookingStatus.WAITING)
            .booker(bookerDtoNested)
            .build();

    @Test
    void saveNewBooking_whenInvoked_thenResponseStatusOkWithBookingDtoInBodyTest() throws Exception {
        when(service.save(anyLong(), any(Booking.class), anyLong()))
                .thenReturn(booking);

        mockMvc.perform(post("/bookings")
                        .header(headerUserId, booker.getId())
                        .content(mapper.writeValueAsString(createBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(dateFormat))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(dateFormat))))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.email", is(bookingDto.getBooker().getEmail())))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())));

        verify(service, times(1))
                .save(anyLong(), any(Booking.class), anyLong());
    }

    @Test
    void saveNewBooking_whenHeaderIsNotCorrect_thenResponseStatus500Test() throws Exception {
        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(createBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(service, never())
                .save(anyLong(), any(), anyLong());
    }

    @Test
    void approve_whenInvoked_thenResponseStatusOkWithBookingDtoInBodyTest() throws Exception {
        when(service.approve(booking.getBooker().getId(), booking.getId(), true))
                .thenReturn(booking);

        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId())
                        .header(headerUserId, booker.getId())
                        .param("approved", "true")
                        .content(mapper.writeValueAsString(createBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(dateFormat))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(dateFormat))))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));

        verify(service, times(1))
                .approve(booking.getBooker().getId(), booking.getId(), true);
    }

    @Test
    void getBooking_whenInvoked_thenResponseStatusOkWithBookingDtoInBodyTest() throws Exception {
        when(service.getByIdAndUserId(booking.getId(), booking.getBooker().getId()))
                .thenReturn(booking);

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header(headerUserId, booker.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(dateFormat))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(dateFormat))))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));

        verify(service, times(1))
                .getByIdAndUserId(booking.getId(), booking.getBooker().getId());
    }

    @Test
    void getAllByBooker_whenInvoked_thenResponseStatusOkWithBookingDtoCollectionInBodyTest() throws Exception {
        final BookingRequestParam defaultParams = BookingRequestParam.builder()
                .from(0)
                .size(30)
                .state("ALL")
                .build();
        when(service.getBookingsByBookerId(eq(booker.getId()), eq(defaultParams)))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(headerUserId, booker.getId())
                        .param("state", defaultParams.getState())
                        .param("from", defaultParams.getFrom().toString())
                        .param("size", defaultParams.getSize().toString())
                        .content(mapper.writeValueAsString(createBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().format(dateFormat))))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().format(dateFormat))))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service, times(1))
                .getBookingsByBookerId(eq(booker.getId()), eq(defaultParams));
    }

    @Test
    void getAllByOwner_whenInvoked_thenResponseStatusOkWithBookingDtoCollectionInBodyTest() throws Exception {
        final BookingRequestParam defaultParams = BookingRequestParam.builder()
                .from(0)
                .size(30)
                .state("ALL")
                .build();
        when(service.getBookingsByOwnerId(eq(booking.getItem().getId()), eq(defaultParams)))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/owner")
                        .header(headerUserId, booking.getItem().getId())
                        .param("state", defaultParams.getState())
                        .param("from", defaultParams.getFrom().toString())
                        .param("size", defaultParams.getSize().toString())
                        .content(mapper.writeValueAsString(createBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().format(dateFormat))))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().format(dateFormat))))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service, times(1))
                .getBookingsByOwnerId(eq(booking.getItem().getId()), eq(defaultParams));
    }
}