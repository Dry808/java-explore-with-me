package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.storage.CommentRepository;
import ru.practicum.ewm.event.enums.EventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exceptions.ConflictException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.storage.UserRepository;

import java.util.List;

/**
 * Сервис для работы с комментариями
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Создать комментарий
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        //проверка, что user существует
        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId))
        );
        // Проверка существования события
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(eventId))
        );
        // Проверка, что событие опубликовано
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя комментировать неопубликованный комментарий");
        }

        return CommentMapper.toDto(repository.save(Comment.builder()
                .text(newCommentDto.getText())
                .author(author)
                .event(event)
                .build()));
    }

    // Получение комментариев для события
    @Override
    public List<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        // проверка события
        eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(eventId))
        );

        // пагинация
        Pageable pageable = PageRequest.of(from / size, size);

        Page<Comment> page = repository.findByEventIdAndIsModeratedTrue(eventId, pageable);

        return page.getContent().stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    // Обновления комментария
    @Override
    public CommentDto updateComment(Long userId, Long eventId, UpdateCommentDto comment) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id=%d не найден".formatted(userId))
        );

        eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(eventId))
        );

        Comment oldComment = repository.findById(comment.getCommentId()).orElseThrow(
                () -> new NotFoundException("Comment с id=%d не найден".formatted(comment.getCommentId()))
        );
        // проверка, что пользователь автор комментария
        if (!oldComment.getAuthor().equals(author)) {
            throw new ConflictException("User с id=%d не является автором комментария с id=%d".formatted(userId,
                    comment.getCommentId()));
        }
        // обновить текст если обновился
        if (!oldComment.getText().equals(comment.getText())) {
            oldComment.setText(comment.getText());
        }

        return CommentMapper.toDto(repository.save(oldComment));
    }

    // модерация
    @Override
    public void moderateComment(Long commentId) {
        Comment comment = repository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментарий с id=%d не найден".formatted(commentId))
        );

        if (comment.getIsModerated()) {
            throw new ConflictException("Комментарий с id=%d был уже модерирован".formatted(commentId));
        }

        comment.setIsModerated(true); // указываем, что прошёл модерацию
        repository.save(comment);
    }

    // удаление комментария(admin)
    @Override
    public void deleteComment(Long commentId) {
        repository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment с id=%d не найден".formatted(commentId))
        );

        repository.deleteById(commentId);
    }

    // удаление комментария пользователем
    @Override
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User с id=%d не найден".formatted(userId))
        );

        eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event с id=%d не найден".formatted(eventId))
        );

        Comment comment = repository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment с id=%d не найден".formatted(commentId))
        );

        if (!comment.getAuthor().equals(author)) {
            throw new ConflictException("User с id=%d не является автором комментария с id=%d".formatted(userId, commentId));
        }

        repository.deleteById(commentId);
    }
}
