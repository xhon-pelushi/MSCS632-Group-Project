package com.todo.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Task {

    private final UUID id;
    private String title;
    private String description;
    private TaskCategory category;
    private TaskStatus status;
    private UUID assignedUserId;
    private final UUID createdByUserId;
    private final LocalDateTime createdAt;

    public Task(UUID id,
                String title,
                String description,
                TaskCategory category,
                TaskStatus status,
                UUID assignedUserId,
                UUID createdByUserId,
                LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = validateTitle(title);
        this.description = sanitizeDescription(description);
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.assignedUserId = Objects.requireNonNull(assignedUserId, "assignedUserId must not be null");
        this.createdByUserId = Objects.requireNonNull(createdByUserId, "createdByUserId must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = validateTitle(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = sanitizeDescription(description);
    }

    public TaskCategory getCategory() {
        return category;
    }

    public void setCategory(TaskCategory category) {
        this.category = Objects.requireNonNull(category, "category must not be null");
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public UUID getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(UUID assignedUserId) {
        this.assignedUserId = Objects.requireNonNull(assignedUserId, "assignedUserId must not be null");
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private static String validateTitle(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Task title must not be empty.");
        }
        return value.trim();
    }

    private static String sanitizeDescription(String value) {
        return value == null ? "" : value.trim();
    }
}
