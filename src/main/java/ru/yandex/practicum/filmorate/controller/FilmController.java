package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateReleaseDate(film.getReleaseDate());

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: id={}, name='{}'", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации при обновлении фильма: не указан id");
            throw new ValidationException("Id фильма должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Ошибка обновления: фильм с id={} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        validateReleaseDate(newFilm.getReleaseDate());

        films.put(newFilm.getId(), newFilm);
        log.info("Обновлён фильм с id={}: {}", newFilm.getId(), newFilm);
        return newFilm;
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Ошибка валидации даты релиза: {} раньше 28.12.1895", releaseDate);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
