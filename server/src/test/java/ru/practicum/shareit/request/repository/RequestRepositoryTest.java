package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static java.time.LocalDateTime.of;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    UserRepository userRepository;

    User user1;
    User user2;
    ItemRequest itemRequest1;
    ItemRequest itemRequest2;

    @BeforeEach
    void beforeEach() {
        user1 = userRepository.save(User.builder()
                .email("user1@email")
                .name("user1")
                .build());
        user2 = userRepository.save(User.builder()
                .email("user2@email")
                .name("user2")
                .build());
        itemRequest1 = requestRepository.save(ItemRequest.builder()
                .description("ItemDescription1")
                .created(of(2020, 1, 1, 0, 0, 1))
                .requestor(user1)
                .build());
        itemRequest2 = requestRepository.save(ItemRequest.builder()
                .description("ItemDescription2")
                .created(of(2021, 1, 1, 0, 0, 1))
                .requestor(user2)
                .build());
    }

    @Test
    void findByRequestorIdOrderByCreatedDescTest() {
        final List<ItemRequest> byRequestor = requestRepository.findByRequestorIdOrderByCreatedDesc(user1.getId());

        assertNotNull(byRequestor);
        assertEquals(List.of(itemRequest1), byRequestor);
    }

    @Test
    void findByRequestorIdNotOrderByCreatedDescTest() {
        final PageRequest pr = PageRequest.of(0, 30);
        final Page<ItemRequest> byNotRequestor = requestRepository.findByRequestorIdNotOrderByCreatedDesc(user1.getId(), pr);

        assertNotNull(byNotRequestor);
        assertEquals(List.of(itemRequest2), byNotRequestor.getContent());
     }

    @AfterEach
    void afterEach() {
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }
}