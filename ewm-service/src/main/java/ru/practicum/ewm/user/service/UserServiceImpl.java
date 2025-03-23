package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.storage.UserRepository;

import java.util.List;

/**
 * Сервис для работы с пользователями
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    // получить всех пользователей
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        if (ids != null) {
            return repository.findByIdIn(ids).stream()
                    .skip(from)
                    .limit(size)
                    .map(UserMapper::toDto)
                    .toList();
        }
        // Если ID не указаны, вернуть всех пользователей с пагинацией
        return repository.findAll().stream()
                .skip(from)
                .limit(size)
                .map(UserMapper::toDto)
                .toList();
    }

    // создать
    @Override
    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        return UserMapper.toDto(repository.save(User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build()));
    }

    // удалить
    @Override
    @Transactional
    public void deleteById(Long userId) {
        repository.deleteById(userId);
    }
}
