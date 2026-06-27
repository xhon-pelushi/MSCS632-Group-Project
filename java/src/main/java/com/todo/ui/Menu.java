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
                ensureActiveUser();
                running = showMainMenu();
            } catch (IllegalArgumentException | NoSuchElementException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }

        System.out.println("Application closed.");
    }

    private void ensureActiveUser() {
        while (activeUser == null) {
            System.out.println();
            System.out.println("Select User");
            System.out.println("1. Existing User");
            System.out.println("2. Create User");

            int choice = readMenuChoice(1, 2);
            if (choice == 1) {
                loginExistingUser();
            } else {
                createAndLoginUser();
            }
        }
    }

    private boolean showMainMenu() {
        System.out.println();
        System.out.println("Active User: " + activeUser.getName() + " [" + shortId(activeUser.getId()) + "]");
        System.out.println("1. View All Tasks");
        System.out.println("2. View My Tasks");
        System.out.println("3. Add Task");
        System.out.println("4. Edit Task");
        System.out.println("5. Mark Task Complete");
        System.out.println("6. Delete Task");
        System.out.println("7. Filter By Category");
        System.out.println("8. Filter By Status");
        System.out.println("9. Switch User");
        System.out.println("10. Run Concurrency Demo");
        System.out.println("0. Exit");

        int choice = readMenuChoice(0, 10);
        switch (choice) {
            case 1 -> viewAllTasks();
            case 2 -> viewMyTasks();
            case 3 -> addTask();
            case 4 -> editTask();
            case 5 -> markTaskComplete();
            case 6 -> deleteTask();
            case 7 -> filterByCategory();
            case 8 -> filterByStatus();
            case 9 -> switchUser();
            case 10 -> runConcurrencyDemo();
            case 0 -> {
                return false;
            }
            default -> throw new IllegalArgumentException("Invalid menu choice.");
        }
        return true;
    }

    private void loginExistingUser() {
        if (!userManager.hasUsers()) {
            System.out.println("No users found. Please create a user first.");
            createAndLoginUser();
            return;
        }

        displayUsers();
        UUID userId = readUuid("Enter user UUID: ");
        activeUser = userManager.getUserById(userId);
        System.out.println("Logged in as " + activeUser.getName() + ".");
    }

    private void createAndLoginUser() {
        String name = readRequiredText("Enter user name: ");
        activeUser = userManager.createUser(name);
        System.out.println("User created. Logged in as " + activeUser.getName() + ".");
    }

    private void switchUser() {
        activeUser = null;
        ensureActiveUser();
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
