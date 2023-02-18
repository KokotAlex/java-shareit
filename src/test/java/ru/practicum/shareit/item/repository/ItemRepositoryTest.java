package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    User user1;
    Item item1;

    @BeforeEach
    void beforeEach() {
        user1 = userRepository.save(User.builder()
                        .id(1L)
                        .email("user1@email")
                        .name("user1")
                        .build());
        item1 = itemRepository.save(Item.builder()
                        .id(1L)
                        .available(true)
                        .owner(user1)
                        .name("item1")
                        .description("item1Description")
                        .build());
    }

    @Test
    void findByOwnerOrderById() {
        final List<Item> byOwner = itemRepository.findByOwnerOrderById(user1);
        assertNotNull(byOwner);
        assertEquals(List.of(item1), byOwner);
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}