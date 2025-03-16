package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.storage.UserRepository;

import java.util.List;

/**
 * Сервис для работы с user
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    // поиск всех пользователей с фильтрацией
    @Override
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        if (ids != null) { // если указаны ID - фильтруем по id
            return repository.findByIdIn(ids).stream()
                    .skip(from)
                    .limit(size)
                    .map(UserMapper::toDto)
                    .toList();
        }

        return repository.findAll().stream()
                .skip(from)
                .limit(size)
                .map(UserMapper::toDto)
                .toList();
    }

    // создать пользователя
    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        return UserMapper.toDto(repository.save(User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build()));
    }

    // удалить пользователя по ID
    @Override
    public void deleteById(Long userId) {
        repository.deleteById(userId);
    }
}
