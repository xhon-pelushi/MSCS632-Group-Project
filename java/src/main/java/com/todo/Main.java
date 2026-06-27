package com.todo;

import com.todo.service.TaskManager;
import com.todo.service.UserManager;
import com.todo.ui.Menu;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        TaskManager taskManager = new TaskManager();
        Menu menu = new Menu(userManager, taskManager);
        menu.start();
    }
}
