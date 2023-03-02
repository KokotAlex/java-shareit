package ru.practicum.shareit.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String objectName, Long id) {
        super(objectName + "  with id: " + id + " does not exist");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
