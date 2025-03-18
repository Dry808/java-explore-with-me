package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.UserInDto;
import ru.practicum.ewm.user.dto.UserOutDto;

import java.util.List;

public interface UserService {
    List<UserOutDto> get(List<Long> ids, Integer from, Integer size);

    UserOutDto create(UserInDto inDto);

    void delete(Long userId);

}