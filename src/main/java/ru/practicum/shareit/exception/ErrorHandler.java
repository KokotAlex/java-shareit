package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handleNotFoundException(NotFoundException exception) {
        log.info("404: {}", exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError("Entity not found", exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException exception) {
        log.info("400: {}", exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("Validation error", exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> incorrectParameterException(Throwable exception) {
        log.info("500: {}", exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("500", exception.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> incorrectParameterException(IncorrectParameterException exception) {
        log.info("409: {}", exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError("Incorrect parameter", exception.getMessage()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ApiError {
        private String message;
        private String debugMessage;
    }


}
