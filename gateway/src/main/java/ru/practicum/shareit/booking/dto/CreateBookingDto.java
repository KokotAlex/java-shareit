package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDto {

	@NotNull(message = "Идентификатор заказываемой вещи должен быть указан.")
	@Min(value = 1, message = "Идентификатор заказываемой вещи должен быть положительным.")
	private long itemId;

	@FutureOrPresent(message = "Дата начала бронирования должна быть больше или ровна текущей даты")
	private LocalDateTime start;

	@Future(message = "Дата окончания бронирования должна быть больше текущей даты")
	private LocalDateTime end;
}
