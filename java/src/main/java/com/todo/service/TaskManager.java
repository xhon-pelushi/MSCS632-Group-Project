package com.todo.service;

import com.todo.model.Task;
import com.todo.model.TaskCategory;
import com.todo.model.TaskStatus;
import com.todo.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class TaskManager {

    private final ConcurrentHashMap<UUID, Task> tasks = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Task addTask(String title,
                        String description,
                        TaskCategory category,
                        UUID assignedUserId,
                        UUID createdByUserId) {
        lock.lock();
        try {
            Task task = new Task(
                    IdGenerator.newId(),
                    title,
                    description,
                    category,
                    TaskStatus.PENDING,
                    assignedUserId,
                    createdByUserId,
                    LocalDateTime.now()
            );
            tasks.put(task.getId(), task);
            return task;
        } finally {
            lock.unlock();
        }
    }

    public void editTask(UUID taskId,
                         String title,
                         String description,
                         TaskCategory category,
                         TaskStatus status,
                         UUID assignedUserId) {
        lock.lock();
        try {
            Task task = requireTask(taskId);
            task.setTitle(title);
            task.setDescription(description);
            task.setCategory(category);
            task.setStatus(status);
            task.setAssignedUserId(assignedUserId);
        } finally {
            lock.unlock();
        }
    }

    public void markTaskComplete(UUID taskId) {
        lock.lock();
        try {
            Task task = requireTask(taskId);
            task.setStatus(TaskStatus.COMPLETED);
        } finally {
            lock.unlock();
        }
    }

    public void deleteTask(UUID taskId) {
        lock.lock();
        try {
            if (tasks.remove(taskId) == null) {
                throw new NoSuchElementException("Task not found: " + taskId);
            }
        } finally {
            lock.unlock();
        }
    }

    public Task getTaskById(UUID taskId) {
        lock.lock();
        try {
            return requireTask(taskId);
        } finally {
            lock.unlock();
        }
    }

    public List<Task> getAllTasks() {
        lock.lock();
        try {
            return sortedSnapshot(tasks.values());
        } finally {
            lock.unlock();
        }
    }

    public List<Task> getTasksByAssignedUser(UUID userId) {
        lock.lock();
        try {
            List<Task> result = new ArrayList<>();
            for (Task task : tasks.values()) {
                if (task.getAssignedUserId().equals(userId)) {
                    result.add(task);
                }
            }
            return sortedSnapshot(result);
        } finally {
            lock.unlock();
        }
    }

    public List<Task> getTasksByCategory(TaskCategory category) {
        lock.lock();
        try {
            List<Task> result = new ArrayList<>();
            for (Task task : tasks.values()) {
                if (task.getCategory() == category) {
                    result.add(task);
                }
            }
            return sortedSnapshot(result);
        } finally {
            lock.unlock();
        }
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        lock.lock();
        try {
            List<Task> result = new ArrayList<>();
            for (Task task : tasks.values()) {
                if (task.getStatus() == status) {
                    result.add(task);
                }
            }
            return sortedSnapshot(result);
        } finally {
            lock.unlock();
        }
    }

    public boolean hasTasks() {
        lock.lock();
        try {
            return !tasks.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public String runConcurrencyDemo(UUID demoUserId) {
        List<UUID> createdTaskIds = new ArrayList<>();

        Thread adderThread = new Thread(() -> {
            for (int index = 1; index <= 5; index++) {
                Task task = addTask(
                        "Demo Task " + index,
                        "Created by Thread A",
                        TaskCategory.WORK,
                        demoUserId,
                        demoUserId
                );
                synchronized (createdTaskIds) {
                    createdTaskIds.add(task.getId());
                }
                sleepQuietly(60L);
            }
        }, "Thread-A");

        Thread completerThread = new Thread(() -> {
            int completedCount = 0;
            while (completedCount < 5) {
                UUID taskId = null;
                synchronized (createdTaskIds) {
                    if (completedCount < createdTaskIds.size()) {
                        taskId = createdTaskIds.get(completedCount);
                        completedCount++;
                    }
                }
                if (taskId != null) {
                    markTaskComplete(taskId);
                } else {
                    sleepQuietly(40L);
                }
            }
        }, "Thread-B");

        adderThread.start();
        completerThread.start();

        try {
            adderThread.join();
            completerThread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Concurrency demo was interrupted.", exception);
        }

        int completedTasks = 0;
        for (UUID taskId : createdTaskIds) {
            if (getTaskById(taskId).getStatus() == TaskStatus.COMPLETED) {
                completedTasks++;
            }
        }

        return "Concurrency demo finished. Created " + createdTaskIds.size()
                + " tasks and completed " + completedTasks + " tasks safely.";
    }

    private Task requireTask(UUID taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new NoSuchElementException("Task not found: " + taskId);
        }
        return task;
    }

    private List<Task> sortedSnapshot(Iterable<Task> source) {
        List<Task> result = new ArrayList<>();
        for (Task task : source) {
            result.add(task);
        }
        result.sort(Comparator.comparing(Task::getCreatedAt).thenComparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
