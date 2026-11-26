package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private Validator validator;
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        filmController = new FilmController();
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический эпос о путешествиях во времени и пространстве.");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        return film;
    }

    @Test
    void shouldPassValidationWhenFilmIsValid() {
        Film film = createValidFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Валидный фильм не должен вызывать ошибок");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = createValidFilm();
        film.setName("   ");
        assertFalse(validator.validate(film).isEmpty(), "Пустое название фильма не должно проходить валидацию");
    }

    @Test
    void shouldValidateDescriptionLengthBoundaryConditions() {
        Film film = createValidFilm();

        film.setDescription("A".repeat(200));
        assertTrue(validator.validate(film).isEmpty(), "Описание длиной ровно 200 символов должно быть валидным");

        film.setDescription("A".repeat(201));
        assertFalse(validator.validate(film).isEmpty(), "Описание длиной 201 символ не должно проходить валидацию");
    }

    @Test
    void shouldValidateReleaseDateBoundaryConditions() {
        Film film = createValidFilm();

        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> filmController.create(film), "Дата 28.12.1895 должна быть валидной");

        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Дата раньше 28.12.1895 должна вызывать исключение");
    }

    @Test
    void shouldValidateDurationBoundaryConditions() {
        Film film = createValidFilm();

        film.setDuration(0);
        assertFalse(validator.validate(film).isEmpty(), "Нулевая продолжительность не должна проходить валидацию");

        film.setDuration(-1);
        assertFalse(validator.validate(film).isEmpty(), "Отрицательная продолжительность не должна проходить валидацию");

        film.setDuration(1);
        assertTrue(validator.validate(film).isEmpty(), "Продолжительность 1 минута должна быть валидной");
    }

    @Test
    void shouldFailOnEmptyFilmObject() {
        Film emptyFilm = new Film();
        Set<ConstraintViolation<Film>> violations = validator.validate(emptyFilm);
        assertFalse(violations.isEmpty(), "Пустой объект Film не должен проходить валидацию");
    }
}
