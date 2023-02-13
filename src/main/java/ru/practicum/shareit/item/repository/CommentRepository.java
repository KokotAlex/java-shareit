package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.practicum.shareit.item.model.Comment;

@RepositoryRestResource
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
