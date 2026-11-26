package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private Validator validator;
    private UserController userController;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userController = new UserController();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1995, 5, 15));
        return user;
    }

    @Test
    void shouldPassValidationWhenUserIsValid() {
        User user = createValidUser();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен вызывать ошибок");
    }

    @Test
    void shouldFailWhenEmailIsBlankOrInvalid() {
        User user = createValidUser();
        user.setEmail("   ");
        assertFalse(validator.validate(user).isEmpty(), "Пустой email не должен проходить валидацию");

        user.setEmail("invalid-email-without-at.ru");
        assertFalse(validator.validate(user).isEmpty(), "Email без символа @ не должен проходить валидацию");
    }

    @Test
    void shouldFailWhenLoginIsBlankOrContainsSpaces() {
        User user = createValidUser();
        user.setLogin("");
        assertFalse(validator.validate(user).isEmpty(), "Пустой логин не должен проходить валидацию");

        user.setLogin("login with space");
        assertThrows(ValidationException.class, () -> userController.create(user),
                "Логин с пробелом должен вызывать исключение в контроллере");
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsEmpty() {
        User user = createValidUser();
        user.setName("");

        User created = userController.create(user);
        assertEquals("validLogin", created.getName(), "Если имя пустое, должен подставляться логин");
    }

    @Test
    void shouldValidateBirthdayBoundaryConditions() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now());
        assertTrue(validator.validate(user).isEmpty(), "Сегодняшняя дата рождения должна быть валидной");

        user.setBirthday(LocalDate.now().plusDays(1));
        assertFalse(validator.validate(user).isEmpty(), "Дата рождения в будущем не должна проходить валидацию");
    }

    @Test
    void shouldFailOnEmptyUserObject() {
        User emptyUser = new User();
        Set<ConstraintViolation<User>> violations = validator.validate(emptyUser);
        assertFalse(violations.isEmpty(), "Пустой объект User не должен проходить валидацию");
    }
}
