package ru.practicum.shareit;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class MyPageRequest extends PageRequest {
    int from;

    public MyPageRequest(int from, int size, Sort sort) {
        super(from / size, size, sort);
        this.from = from;
    }

    @Override
    public long getOffset() {
        return from;
    }
}
