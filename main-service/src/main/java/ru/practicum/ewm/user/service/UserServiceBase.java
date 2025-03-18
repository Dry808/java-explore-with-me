package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.UserMapper;
import ru.practicum.ewm.user.UserRepository;
import ru.practicum.ewm.user.dto.UserInDto;
import ru.practicum.ewm.user.dto.UserOutDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceBase implements UserService  {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserOutDto create(UserInDto  userInDto) {
        return UserMapper.toUserOutDto(userRepository.save(UserMapper.toUser(userInDto)));
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id= " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserOutDto> get(List<Long>  listId, Integer from, Integer size) {
        List<UserOutDto>  listUserOutDto;

        PageRequest page = PageRequest.of(from / size, size);
        if (listId != null) {
            listUserOutDto = userRepository.findByIdIn(listId, page).stream()
                    .map(UserMapper::toUserOutDto)
                    .collect(Collectors.toList());
        } else {
            listUserOutDto = userRepository.findAll(page).stream()
                    .map(UserMapper::toUserOutDto)
                    .collect(Collectors.toList());
        }
        return listUserOutDto;
    }

}