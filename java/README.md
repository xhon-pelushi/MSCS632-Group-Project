# Collaborative To-Do List Application

Java 21 console application for collaborative task management using an object-oriented design and in-memory storage.

## Features

- Create users and switch the active user session
- Add, edit, delete, and complete tasks
- Assign tasks to any existing user
- View all tasks or only tasks assigned to the current user
- Filter tasks by category or status
- Run a built-in concurrency demonstration with two threads
- Validate invalid menu input, UUIDs, and empty task titles without crashing

## Architecture

```text
com.todo
├── Main
├── model
│   ├── Task
│   ├── TaskCategory
│   ├── TaskStatus
│   └── User
├── service
│   ├── TaskManager
│   └── UserManager
├── ui
│   └── Menu
└── util
    └── IdGenerator
```

## Technical Notes

- Java version: 21
- Build tool: Maven
- Storage: `ConcurrentHashMap<UUID, User>` and `ConcurrentHashMap<UUID, Task>`
- Concurrency control: `ReentrantLock` inside `TaskManager`
- Time model: `LocalDateTime`
- UI type: terminal / console only

## Build

From the `java/` directory:

```bash
mvn clean package
```

## Run

From the `java/` directory:

```bash
mvn exec:java
```

## Sample Workflow

1. Create a user or log in as an existing user.
2. Add tasks and assign them to users.
3. View all tasks or only tasks assigned to the active user.
4. Edit task details or mark tasks as completed.
5. Run menu option `10` to execute the concurrency demo.
