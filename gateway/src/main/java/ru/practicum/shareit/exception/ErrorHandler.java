package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException exception) {
        log.error("400: {}", exception.getMessage(), exception);

        return new ApiError("Validation error", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalException(Throwable exception) {
        log.error("500: {}", exception.getMessage(), exception);

        return new ApiError("500", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError incorrectParameterException(BadRequestException exception) {
        log.error("400: {}", exception.getMessage(), exception);

        return new ApiError("Bad Request", exception.getMessage());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ApiError {
        private String message;
        private String error;
    }
}