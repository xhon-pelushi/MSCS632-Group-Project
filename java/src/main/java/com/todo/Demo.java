package com.todo;

import com.todo.model.Task;
import com.todo.model.TaskCategory;
import com.todo.model.TaskStatus;
import com.todo.model.User;
import com.todo.service.TaskManager;
import com.todo.service.UserManager;

import java.util.List;

/**
 * Demo entry point — produces focused, screenshot-friendly output for each
 * feature scene without interactive input. Usage:
 *   mvn exec:java -Dexec.mainClass=com.todo.Demo -Dexec.args=<scene>
 * Scenes: users | menu | add | tasks | filter | concurrency
 */
public final class Demo {

    private static final String HR = "─".repeat(52);

    private Demo() {}

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.err.println("Usage: Demo <users|menu|add|tasks|filter|concurrency>");
            System.exit(1);
        }

        UserManager userManager = new UserManager();
        TaskManager taskManager = new TaskManager();

        User xhon  = userManager.createUser("Xhon Pelushi");
        User sikun = userManager.createUser("Sikun Peng");

        switch (args[0]) {

            case "users" -> {
                header("Collaborative To-Do List");
                System.out.println("\n  Select a user:\n");
                List<User> users = userManager.getAllUsers();
                for (int i = 0; i < users.size(); i++) {
                    System.out.printf("    %d. %s%n", i + 1, users.get(i).getName());
                }
                System.out.printf("    %d. Create new user%n", users.size() + 1);
                System.out.println("    0. Exit\n");
                System.out.println("  Enter choice: _");
            }

            case "menu" -> {
                header("Active User: " + xhon.getName());
                System.out.println("\n  1. View All Tasks");
                System.out.println("  2. View My Tasks");
                System.out.println("  3. Add Task");
                System.out.println("  4. Edit Task");
                System.out.println("  5. Mark Task Complete");
                System.out.println("  6. Delete Task");
                System.out.println("  7. Filter By Category");
                System.out.println("  8. Filter By Status");
                System.out.println("  9. Switch User");
                System.out.println(" 10. Run Concurrency Demo");
                System.out.println("  0. Exit\n");
                System.out.println("  Enter choice: _");
            }

            case "add" -> {
                header("Add Task");
                System.out.println("\n  Enter task title:       Fix login bug");
                System.out.println("  Enter task description: Users cannot log in after password reset");
                System.out.println("\n  Select category:");
                System.out.println("    1. WORK");
                System.out.println("    2. PERSONAL");
                System.out.println("    3. SHOPPING");
                System.out.println("    4. OTHER");
                System.out.println("  Enter choice: 1");
                System.out.println("\n  Available Users");
                System.out.printf("  %-36s %-20s%n", "UUID", "Name");
                System.out.println("  " + "─".repeat(58));
                System.out.printf("  %-36s %-20s%n", xhon.getId(), xhon.getName());
                System.out.printf("  %-36s %-20s%n", sikun.getId(), sikun.getName());
                System.out.println("\n  Enter assigned user UUID: " + xhon.getId());
                Task t = taskManager.addTask("Fix login bug",
                        "Users cannot log in after password reset",
                        TaskCategory.WORK, xhon.getId(), xhon.getId());
                System.out.println("\n  Task added with ID: " + t.getId());
            }

            case "tasks" -> {
                seedTasks(taskManager, xhon, sikun);
                List<Task> all = taskManager.getAllTasks();
                taskManager.editTask(all.get(1).getId(), all.get(1).getTitle(),
                        all.get(1).getDescription(), all.get(1).getCategory(),
                        TaskStatus.COMPLETED, all.get(1).getAssignedUserId());
                taskManager.editTask(all.get(3).getId(), all.get(3).getTitle(),
                        all.get(3).getDescription(), all.get(3).getCategory(),
                        TaskStatus.IN_PROGRESS, all.get(3).getAssignedUserId());
                header("All Tasks");
                printTaskTable(taskManager.getAllTasks(), userManager);
            }

            case "filter" -> {
                seedTasks(taskManager, xhon, sikun);
                List<Task> all = taskManager.getAllTasks();
                taskManager.markTaskComplete(all.get(0).getId());
                taskManager.markTaskComplete(all.get(2).getId());
                header("Filter → Status: COMPLETED");
                printTaskTable(taskManager.getTasksByStatus(TaskStatus.COMPLETED), userManager);
            }

            case "concurrency" -> {
                seedTasks(taskManager, xhon, sikun);
                header("Concurrency Demo");
                System.out.println("\n  Launching Thread-A (adds tasks) and Thread-B (completes them)");
                System.out.println("  Both threads share a TaskManager guarded by ReentrantLock.\n");
                String result = taskManager.runConcurrencyDemo(xhon.getId());
                System.out.println("  " + result);
                System.out.println("\n  ReentrantLock serializes all writes — no data corruption");
                System.out.println("  even when Thread-A and Thread-B race to modify the same list.");
            }

            default -> {
                System.err.println("Unknown scene: " + args[0]);
                System.exit(1);
            }
        }
    }

    private static void seedTasks(TaskManager tm, User xhon, User sikun) {
        tm.addTask("Fix login bug",       "Users cannot log in after password reset", TaskCategory.WORK,     xhon.getId(),  xhon.getId());
        tm.addTask("Write unit tests",    "Cover TaskManager and UserManager",        TaskCategory.WORK,     sikun.getId(), xhon.getId());
        tm.addTask("Buy groceries",       "",                                          TaskCategory.SHOPPING, xhon.getId(),  xhon.getId());
        tm.addTask("Update README",       "Add build and run instructions",            TaskCategory.WORK,     sikun.getId(), sikun.getId());
        tm.addTask("Review pull request", "Check Sikun's Java implementation",         TaskCategory.WORK,     xhon.getId(),  sikun.getId());
    }

    private static void header(String title) {
        System.out.println(HR);
        System.out.println("  " + title);
        System.out.println(HR);
    }

    private static void printTaskTable(List<Task> tasks, UserManager um) {
        if (tasks.isEmpty()) {
            System.out.println("\n  No tasks found.");
            return;
        }
        System.out.println();
        System.out.printf("  %-3s  %-12s  %-10s  %-24s  %-16s%n",
                "ID", "STATUS", "CATEGORY", "TITLE", "ASSIGNED TO");
        System.out.println("  " + "─".repeat(72));
        for (Task t : tasks) {
            String assignee = um.getUserById(t.getAssignedUserId()).getName();
            System.out.printf("  %-3s  %-12s  %-10s  %-24s  %-16s%n",
                    t.getId().toString().substring(0, 8),
                    t.getStatus(),
                    t.getCategory(),
                    truncate(t.getTitle(), 24),
                    truncate(assignee, 16));
        }
        System.out.println();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
