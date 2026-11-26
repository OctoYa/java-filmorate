package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации при создании пользователя: логин '{}' содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не указано, в качестве имени установлен логин: {}", user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь: id={}, login={}, email={}", user.getId(), user.getLogin(), user.getEmail());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации при обновлении пользователя: не указан id");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка обновления: пользователь с id={} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        if (newUser.getLogin().contains(" ")) {
            log.warn("Ошибка валидации при обновлении пользователя id={}: логин '{}' содержит пробелы", newUser.getId(), newUser.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }

        users.put(newUser.getId(), newUser);
        log.info("Обновлён пользователь с id={}: {}", newUser.getId(), newUser);
        return newUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
