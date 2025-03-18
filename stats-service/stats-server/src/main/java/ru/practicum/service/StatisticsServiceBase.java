package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.*;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsServiceBase implements StatisticsService {
    private final StatRepository statRepository;

    @Override
    @Transactional
    public StatDto create(StatInDto statInDto) {
        Statistics statistics = StatisticsMapper.toStatistics(statInDto);

        return StatisticsMapper.toStatDto(statRepository.save(statistics));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatOutDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        if (start.isAfter(end)) {
            throw new InvalidParameterException("Дата начала start: " + start + "и дата окончания end: " + end + "не могут быть равны или противоречить друг другу");
        }
        if (unique) {
            if (uris != null) {
                return statRepository.findAllWithUniqueIpWithUris(uris, start, end);
            }
            return statRepository.findAllWithUniqueIpWithoutUris(start, end);
        } else {
            if (uris != null) {
                return statRepository.findAllWithUris(uris, start, end);
            }
            return statRepository.findAllWithoutUris(start, end);
        }
    }
}