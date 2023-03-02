package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@RepositoryRestResource
public interface RequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long userId);

    Page<ItemRequest>  findByRequestorIdNotOrderByCreatedDesc(Long userId, Pageable pr);

}
