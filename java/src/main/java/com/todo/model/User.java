package com.todo.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final String name;
    private final LocalDateTime createdAt;

    public User(UUID id, String name, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = validateName(name);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private static String validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User name must not be empty.");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
