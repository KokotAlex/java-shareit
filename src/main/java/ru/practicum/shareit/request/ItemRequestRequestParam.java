package ru.practicum.shareit.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ItemRequestRequestParam {
    private Integer from;
    private Integer size;
}
