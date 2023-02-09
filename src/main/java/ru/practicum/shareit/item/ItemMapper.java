package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item, Long userId) {

        Optional<Booking> lastBookingOptional = getLastBooking(item, userId);
        Optional<Booking> nextBookingOptional = getNextBooking(item, userId);

        Set<CommentDto.Nested> comments = item.getComments().stream()
                .map(ItemMapper::toCommentDtoShort)
                .collect(Collectors.toSet());

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBookingOptional.map(BookingMapper::toBookingDtoShort).orElse(null))
                .nextBooking(nextBookingOptional.map(BookingMapper::toBookingDtoShort).orElse(null))
                .comments(comments)
                .build();
    }

    public static ItemDto.Nested toItemDtoShort(Item item, Long userId) {

        Optional<Booking> lastBookingOptional = getLastBooking(item, userId);
        Optional<Booking> nextBookingOptional = getNextBooking(item, userId);

        return ItemDto.Nested.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBookingId(lastBookingOptional.map(Booking::getId).orElse(null))
                .nextBookingId(nextBookingOptional.map(Booking::getId).orElse(null))
                .build();
    }

    public static Item toItem(ItemDto item) {
        return Item.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .created(commentDto.getCreated())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static CommentDto.Nested toCommentDtoShort(Comment comment) {
        return CommentDto.Nested.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    private static Optional<Booking> getLastBooking(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();

        return item.getBookings().stream()
                .filter(booking -> (booking.getEnd().isBefore(now)))
                .max(Comparator.comparing(Booking::getEnd));
    }

    private static Optional<Booking> getNextBooking(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();

        return item.getBookings().stream()
                .filter(booking -> (booking.getEnd().isAfter(now)))
                .min(Comparator.comparing(Booking::getEnd));
    }

}
