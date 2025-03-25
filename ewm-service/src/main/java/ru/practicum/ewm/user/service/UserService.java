package ru.practicum.ewm.user.service;

import jakarta.validation.Valid;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(@Valid NewUserRequest newUserRequest);

    void deleteById(Long userId);
}
