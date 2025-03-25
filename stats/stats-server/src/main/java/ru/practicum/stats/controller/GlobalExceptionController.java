package ru.practicum.stats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.stats.exceptions.InvalidDateException;
import ru.practicum.stats.exceptions.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionController {
    @ExceptionHandler(InvalidDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidDateException(InvalidDateException e) {
        return new ErrorResponse("Ошибка формата даты", e.getMessage());
    }
}
