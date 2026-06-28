package com.todo.service;

import com.todo.model.User;
import com.todo.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserManager {

    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();

    public UserManager() {
        // Pre-seed team members
        createUser("Xhon Pelushi");
        createUser("Sikun Peng");
    }

    public User createUser(String name) {
        User user = new User(IdGenerator.newId(), name, LocalDateTime.now());
        users.put(user.getId(), user);
        return user;
    }

    public User getUserById(UUID userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("User not found: " + userId);
        }
        return user;
    }

    public boolean exists(UUID userId) {
        return users.containsKey(userId);
    }

    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>(users.values());
        result.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(User::getCreatedAt));
        return result;
    }

    public boolean hasUsers() {
        return !users.isEmpty();
    }
}
