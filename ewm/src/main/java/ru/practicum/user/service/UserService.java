package ru.practicum.user.service;

import jakarta.validation.Valid;
import ru.practicum.user.dto.NewUserRequest;

import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(@Valid NewUserRequest newUserRequest);

    void deleteById(Long userId);
}
