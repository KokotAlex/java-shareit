package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Item {
    private Long id;
    private Boolean available;
    private Long ownerId;
    private String name;
    private String description;
}
