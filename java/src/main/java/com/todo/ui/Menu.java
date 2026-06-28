package com.todo.ui;

import com.todo.model.Task;
import com.todo.model.TaskCategory;
import com.todo.model.TaskStatus;
import com.todo.model.User;
import com.todo.service.TaskManager;
import com.todo.service.UserManager;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;

public final class Menu {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserManager userManager;
    private final TaskManager taskManager;
    private final Scanner scanner;

    private User activeUser;

    public Menu(UserManager userManager, TaskManager taskManager) {
        this.userManager = userManager;
        this.taskManager = taskManager;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=============================");
        System.out.println("Collaborative To-Do List");
        System.out.println("=============================");

        boolean running = true;
        while (running) {
            try {
                if (activeUser == null && !selectUser()) {
                    running = false;
                } else {
                    running = showMainMenu();
                }
            } catch (IllegalArgumentException | NoSuchElementException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }

        System.out.println("Application closed.");
    }

    private boolean selectUser() {
        while (activeUser == null) {
            List<User> users = userManager.getAllUsers();
            String rule = Ansi.ORANGE + "━".repeat(44) + Ansi.RESET;
            System.out.println();
            System.out.println(rule);
            System.out.println("  " + Ansi.ORANGE + Ansi.BOLD + "☕ JAVA" + Ansi.RESET
                    + Ansi.GRAY + "  ·  " + Ansi.RESET + Ansi.BOLD + "Select a user" + Ansi.RESET);
            System.out.println(rule);
            for (int index = 0; index < users.size(); index++) {
                System.out.println("   " + Ansi.CYAN + Ansi.BOLD + "[" + (index + 1) + "]" + Ansi.RESET
                        + " " + users.get(index).getName());
                Ansi.sleep(30);
            }
            System.out.println("   " + Ansi.CYAN + Ansi.BOLD + "[" + (users.size() + 1) + "]" + Ansi.RESET
                    + " Add new user");
            System.out.println("   " + Ansi.CYAN + Ansi.BOLD + "[0]" + Ansi.RESET + " Exit");
            System.out.println(rule);

            int choice = readMenuChoice(0, users.size() + 1);
            if (choice == 0) {
                return false;
            }
            Ansi.sweep(Ansi.ORANGE);
            if (choice == users.size() + 1) {
                createAndLoginUser();
            } else {
                activeUser = users.get(choice - 1);
                System.out.println("Logged in as " + activeUser.getName() + ".");
            }
        }
        return true;
    }

    private boolean showMainMenu() {
        printMainMenu();
        int choice = readMenuChoice(0, 9);
        if (choice == 0) {
            return false;
        }
        Ansi.sweep(Ansi.ORANGE);
        switch (choice) {
            case 1 -> viewAllTasks();
            case 2 -> viewMyTasks();
            case 3 -> addTask();
            case 4 -> editTask();
            case 5 -> markTaskComplete();
            case 6 -> deleteTask();
            case 7 -> filterTasks();
            case 8 -> runConcurrencyDemo();
            case 9 -> switchUser();
            default -> throw new IllegalArgumentException("Invalid menu choice.");
        }
        return true;
    }

    private void printMainMenu() {
        String[] options = {
                "[1] View all tasks",
                "[2] View my tasks",
                "[3] Add task",
                "[4] Edit task",
                "[5] Mark task complete",
                "[6] Delete task",
                "[7] Filter tasks",
                "[8] Concurrency demo",
                "[9] Switch user",
                "[0] Exit",
        };
        String rule = Ansi.ORANGE + "━".repeat(44) + Ansi.RESET;
        System.out.println();
        System.out.println(rule);
        System.out.println("  " + Ansi.ORANGE + Ansi.BOLD + "☕ JAVA" + Ansi.RESET
                + Ansi.GRAY + "  ·  " + Ansi.RESET
                + Ansi.BOLD + "Active User: " + Ansi.RESET + activeUser.getName());
        System.out.println(rule);
        for (String option : options) {
            System.out.println("   " + Ansi.CYAN + Ansi.BOLD + option.substring(0, 3) + Ansi.RESET
                    + option.substring(3));
            Ansi.sleep(30);
        }
        System.out.println(rule);
    }

    private void createAndLoginUser() {
        String name = readRequiredText("Enter user name: ");
        activeUser = userManager.createUser(name);
        System.out.println("User created. Logged in as " + activeUser.getName() + ".");
    }

    private void switchUser() {
        activeUser = null;
    }

    private void viewAllTasks() {
        printTasks(taskManager.getAllTasks());
    }

    private void viewMyTasks() {
        printTasks(taskManager.getTasksByAssignedUser(activeUser.getId()));
    }

    private void addTask() {
        String title = readRequiredText("Enter task title: ");
        String description = readOptionalText("Enter task description: ");
        TaskCategory category = readCategory();
        UUID assignedUserId = readAssignedUserId();

        Task task = taskManager.addTask(title, description, category, assignedUserId, activeUser.getId());
        System.out.println("Task added with ID: " + task.getId());
    }

    private void editTask() {
        Task task = selectTask("Enter task UUID to edit: ");
        System.out.println("Editing task: " + task.getTitle());

        String title = readRequiredText("Enter new title: ");
        String description = readOptionalText("Enter new description: ");
        TaskCategory category = readCategory();
        TaskStatus status = readStatus();
        UUID assignedUserId = readAssignedUserId();

        taskManager.editTask(task.getId(), title, description, category, status, assignedUserId);
        System.out.println("Task updated.");
    }

    private void markTaskComplete() {
        Task task = selectTask("Enter task UUID to mark complete: ");
        taskManager.markTaskComplete(task.getId());
        System.out.println("Task marked as completed.");
    }

    private void deleteTask() {
        Task task = selectTask("Enter task UUID to delete: ");
        taskManager.deleteTask(task.getId());
        System.out.println("Task deleted.");
    }

    private void filterTasks() {
        System.out.println();
        System.out.println("Filter by:");
        System.out.println("1. Category");
        System.out.println("2. Status");
        System.out.println("0. Back");

        int choice = readMenuChoice(0, 2);
        switch (choice) {
            case 1 -> filterByCategory();
            case 2 -> filterByStatus();
            case 0 -> { }
            default -> throw new IllegalArgumentException("Invalid menu choice.");
        }
    }

    private void filterByCategory() {
        TaskCategory category = readCategory();
        printTasks(taskManager.getTasksByCategory(category));
    }

    private void filterByStatus() {
        TaskStatus status = readStatus();
        printTasks(taskManager.getTasksByStatus(status));
    }

    private void runConcurrencyDemo() {
        String message = taskManager.runConcurrencyDemo(activeUser.getId());
        System.out.println(message);
    }

    private Task selectTask(String prompt) {
        if (!taskManager.hasTasks()) {
            throw new NoSuchElementException("No tasks available.");
        }
        printTasks(taskManager.getAllTasks());
        UUID taskId = readUuid(prompt);
        return taskManager.getTaskById(taskId);
    }

    private UUID readAssignedUserId() {
        displayUsers();
        UUID userId = readUuid("Enter assigned user UUID: ");
        userManager.getUserById(userId);
        return userId;
    }

    private TaskCategory readCategory() {
        TaskCategory[] categories = TaskCategory.values();
        System.out.println("Select category:");
        for (int index = 0; index < categories.length; index++) {
            System.out.println((index + 1) + ". " + categories[index]);
        }
        int choice = readMenuChoice(1, categories.length);
        return categories[choice - 1];
    }

    private TaskStatus readStatus() {
        TaskStatus[] statuses = TaskStatus.values();
        System.out.println("Select status:");
        for (int index = 0; index < statuses.length; index++) {
            System.out.println((index + 1) + ". " + statuses[index]);
        }
        int choice = readMenuChoice(1, statuses.length);
        return statuses[choice - 1];
    }

    private int readMenuChoice(int min, int max) {
        while (true) {
            System.out.print("Enter choice: ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice < min || choice > max) {
                    System.out.println("Invalid menu choice. Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return choice;
            } catch (NumberFormatException exception) {
                System.out.println("Invalid menu choice. Please enter a number.");
            }
        }
    }

    private UUID readUuid(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return UUID.fromString(input);
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid UUID format. Please try again.");
            }
        }
    }

    private String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Value must not be empty.");
        }
    }

    private String readOptionalText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void displayUsers() {
        List<User> users = userManager.getAllUsers();
        System.out.println();
        System.out.println("Available Users");
        System.out.printf("%-36s %-20s %-16s%n", "UUID", "Name", "Created");
        System.out.println("-----------------------------------------------------------------------");
        for (User user : users) {
            System.out.printf(
                    "%-36s %-20s %-16s%n",
                    user.getId(),
                    truncate(user.getName(), 20),
                    user.getCreatedAt().format(DATE_TIME_FORMATTER)
            );
        }
    }

    private void printTasks(List<Task> tasks) {
        System.out.println();
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        System.out.printf("%-8s %-12s %-12s %-12s %-22s %-22s%n",
                "ID", "Status", "User", "Category", "Title", "Created By");
        System.out.println("-----------------------------------------------------------------------------------------------");

        for (Task task : tasks) {
            User assignedUser = userManager.getUserById(task.getAssignedUserId());
            User createdByUser = userManager.getUserById(task.getCreatedByUserId());
            System.out.printf("%-8s %-12s %-12s %-12s %-22s %-22s%n",
                    shortId(task.getId()),
                    task.getStatus(),
                    truncate(assignedUser.getName(), 12),
                    task.getCategory(),
                    truncate(task.getTitle(), 22),
                    truncate(createdByUser.getName(), 22));
        }

        System.out.println();
        for (Task task : tasks) {
            User assignedUser = userManager.getUserById(task.getAssignedUserId());
            System.out.println("Task UUID: " + task.getId());
            System.out.println("Assigned To: " + assignedUser.getName());
            System.out.println("Description: " + (task.getDescription().isBlank() ? "(none)" : task.getDescription()));
            System.out.println("Created At: " + task.getCreatedAt().format(DATE_TIME_FORMATTER));
            System.out.println("-----------------------------------------------------------------------------------------------");
        }
    }

    private String shortId(UUID uuid) {
        return uuid.toString().substring(0, 8);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
