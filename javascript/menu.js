const readline = require('readline');
const { CATEGORY, STATUS } = require('./constants');
const userManager = require('./userManager');
const taskManager = require('./taskManager');

const rl = readline.createInterface({ input: process.stdin, output: process.stdout });

// In piped (non-TTY) mode readline fires all 'line' events synchronously before
// the next ask() can register a listener. Buffer incoming lines and drain them
// into waiting resolvers so no input is lost regardless of mode.
const _lineQueue = [];
const _waiting   = [];

rl.on('line', line => {
  if (_waiting.length > 0) {
    _waiting.shift()(line);
  } else {
    _lineQueue.push(line);
  }
});

function ask(question) {
  process.stdout.write(question);
  return new Promise(resolve => {
    if (_lineQueue.length > 0) {
      resolve(_lineQueue.shift());
    } else {
      _waiting.push(resolve);
    }
  });
}

const IS_TTY = process.stdout.isTTY;

// Keep the startup banner visible: skip the clear on the very first screen so
// the splash stays on screen with the first menu drawn underneath it.
let _skipFirstClear = true;

const C = {
  reset: '\x1b[0m',
  bold: '\x1b[1m',
  yellow: '\x1b[38;5;226m',
  cyan: '\x1b[36m',
  gray: '\x1b[38;5;245m',
  green: '\x1b[32m',
};

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

// Draws a colored bar running left-to-right across the screen, then clears it.
async function sweep(color = C.yellow) {
  const width = 44;
  for (let i = 1; i <= width; i++) {
    process.stdout.write('\r' + color + '━'.repeat(i) + C.reset);
    await sleep(8);
  }
  process.stdout.write('\r' + ' '.repeat(width) + '\r');
}

function hr() { console.log('─'.repeat(52)); }

function header(title) {
  if (IS_TTY && !_skipFirstClear) console.clear();
  _skipFirstClear = false;
  hr();
  console.log(`  ${title}`);
  hr();
}

function taskLine(task) {
  const icon = task.status === STATUS.COMPLETED ? '✓'
             : task.status === STATUS.IN_PROGRESS ? '◌' : '☐';
  const assignee = userManager.getUserById(task.assignedTo);
  const name = assignee ? assignee.name : 'Unassigned';
  return (
    `  [${task.id}] ${icon} ${task.title}\n` +
    `       ${task.category}  |  ${task.status}  |  → ${name}`
  );
}

async function pause() {
  await ask('\n  Press Enter to continue...');
}

// ── Screens ───────────────────────────────────────────────────────────────────

async function userSelectionScreen() {
  while (true) {
    header('Collaborative To-Do List');
    console.log('\n  ' + C.bold + C.yellow + 'Select a user' + C.reset + '\n');

    const users = userManager.getUsers();
    for (let i = 0; i < users.length; i++) {
      console.log('   ' + C.cyan + C.bold + `[${i + 1}]` + C.reset + ' ' + users[i].name);
      await sleep(30);
    }
    console.log('   ' + C.cyan + C.bold + `[${users.length + 1}]` + C.reset + ' Add new user');
    console.log('   ' + C.cyan + C.bold + '[0]' + C.reset + ' Exit\n');

    const choice = (await ask('  ➤ ')).trim();

    if (choice === '0') { shutdown(); }

    const idx = parseInt(choice) - 1;
    if (idx >= 0 && idx < users.length) {
      await sweep();
      userManager.setActiveUser(users[idx].id);
      return;
    }

    if (parseInt(choice) === users.length + 1) {
      const name = (await ask('  Name: ')).trim();
      if (name) {
        await sweep();
        const user = userManager.createUser(name);
        userManager.setActiveUser(user.id);
        return;
      }
    }
  }
}

async function viewTasksScreen(tasks) {
  if (IS_TTY) console.clear();
  header('Tasks');
  if (tasks.length === 0) {
    console.log('\n  No tasks found.');
  } else {
    console.log();
    tasks.forEach(t => console.log(taskLine(t) + '\n'));
  }
  await pause();
}

async function addTaskScreen() {
  if (IS_TTY) console.clear();
  header('Add New Task');

  const title = (await ask('\n  Title: ')).trim();
  if (!title) return;

  const description = (await ask('  Description (optional): ')).trim();

  const cats = Object.values(CATEGORY);
  console.log('\n  Category:');
  cats.forEach((c, i) => console.log(`    ${i + 1}. ${c}`));
  const catChoice = parseInt((await ask('  > ')).trim()) - 1;
  const category = cats[catChoice] ?? CATEGORY.OTHER;

  const users = userManager.getUsers();
  console.log('\n  Assign to:');
  users.forEach((u, i) => console.log(`    ${i + 1}. ${u.name}`));
  const userChoice = parseInt((await ask('  > ')).trim()) - 1;
  const assignedUser = users[userChoice] ?? userManager.getActiveUser();

  const task = await taskManager.addTask({
    title,
    description,
    category,
    assignedTo: assignedUser.id,
    createdBy: userManager.getActiveUser().id,
  });

  console.log(`\n  ✓ Task "${task.title}" added.`);
  await pause();
}

async function editTaskScreen() {
  if (IS_TTY) console.clear();
  header('Edit Task');

  const tasks = taskManager.getAllTasks();
  if (tasks.length === 0) {
    console.log('\n  No tasks to edit.');
    await pause();
    return;
  }

  console.log();
  tasks.forEach(t => console.log(taskLine(t) + '\n'));

  const id = (await ask('  Task ID to edit: ')).trim();
  const task = taskManager.getTaskById(id);
  if (!task) {
    console.log('\n  Task not found.');
    await pause();
    return;
  }

  console.log(`\n  Editing: "${task.title}" (Enter to keep current value)\n`);

  const title       = (await ask(`  Title [${task.title}]: `)).trim();
  const description = (await ask(`  Description [${task.description || 'none'}]: `)).trim();

  const statuses = Object.values(STATUS);
  console.log('\n  Status:');
  statuses.forEach((s, i) => console.log(`    ${i + 1}. ${s}`));
  const statusChoice = parseInt((await ask('  > ')).trim()) - 1;
  const status = statuses[statusChoice] ?? task.status;

  const users = userManager.getUsers();
  console.log('\n  Assign to:');
  users.forEach((u, i) => console.log(`    ${i + 1}. ${u.name}`));
  const userChoice  = parseInt((await ask('  > ')).trim()) - 1;
  const assignedUser = users[userChoice];

  await taskManager.updateTask(id, {
    ...(title       && { title }),
    ...(description && { description }),
    status,
    ...(assignedUser && { assignedTo: assignedUser.id }),
  });

  console.log('\n  ✓ Task updated.');
  await pause();
}

async function deleteTaskScreen() {
  if (IS_TTY) console.clear();
  header('Delete Task');

  const tasks = taskManager.getAllTasks();
  if (tasks.length === 0) {
    console.log('\n  No tasks to delete.');
    await pause();
    return;
  }

  console.log();
  tasks.forEach(t => console.log(taskLine(t) + '\n'));

  const id = (await ask('  Task ID to delete: ')).trim();
  const task = taskManager.getTaskById(id);
  if (!task) {
    console.log('\n  Task not found.');
    await pause();
    return;
  }

  const confirm = (await ask(`  Delete "${task.title}"? (y/n): `)).trim().toLowerCase();
  if (confirm === 'y') {
    await taskManager.deleteTask(id);
    console.log('\n  ✓ Task deleted.');
  } else {
    console.log('\n  Cancelled.');
  }
  await pause();
}

async function markCompleteScreen() {
  if (IS_TTY) console.clear();
  header('Mark Task Complete');

  const tasks = taskManager.getAllTasks().filter(t => t.status !== STATUS.COMPLETED);
  if (tasks.length === 0) {
    console.log('\n  No pending tasks.');
    await pause();
    return;
  }

  console.log();
  tasks.forEach(t => console.log(taskLine(t) + '\n'));

  const id = (await ask('  Task ID to complete: ')).trim();
  const task = taskManager.getTaskById(id);
  if (!task) {
    console.log('\n  Task not found.');
  } else {
    await taskManager.updateTask(id, { status: STATUS.COMPLETED });
    console.log(`\n  ✓ "${task.title}" marked as COMPLETED.`);
  }
  await pause();
}

async function filterScreen() {
  if (IS_TTY) console.clear();
  header('Filter Tasks');
  console.log('\n  Filter by:\n');
  console.log('    1. Category');
  console.log('    2. Status');
  console.log('    0. Back\n');

  const choice = (await ask('  > ')).trim();

  if (choice === '1') {
    const cats = Object.values(CATEGORY);
    console.log('\n  Category:');
    cats.forEach((c, i) => console.log(`    ${i + 1}. ${c}`));
    const c = parseInt((await ask('  > ')).trim()) - 1;
    if (cats[c]) await viewTasksScreen(taskManager.filterByCategory(cats[c]));
  } else if (choice === '2') {
    const statuses = Object.values(STATUS);
    console.log('\n  Status:');
    statuses.forEach((s, i) => console.log(`    ${i + 1}. ${s}`));
    const s = parseInt((await ask('  > ')).trim()) - 1;
    if (statuses[s]) await viewTasksScreen(taskManager.filterByStatus(statuses[s]));
  }
}

async function concurrencyDemoScreen() {
  if (IS_TTY) console.clear();
  header('Concurrency Demo');
  console.log('\n  Simulating concurrent task updates with async/await + mutex...\n');

  const log = await taskManager.simulateConcurrentAccess();
  log.forEach(l => console.log(l));

  console.log('\n  Promise.all fires all operations simultaneously.');
  console.log('  The AsyncMutex queues writers so each update is atomic,');
  console.log('  mirroring Java\'s synchronized blocks on a shared list.\n');
  await pause();
}

function shutdown() {
  console.log('\n  Goodbye!\n');
  rl.close();
  process.exit(0);
}

// ── Main menu loop ────────────────────────────────────────────────────────────

async function mainMenu() {
  while (true) {
    const active = userManager.getActiveUser();
    if (IS_TTY) console.clear();

    const rule = C.yellow + '━'.repeat(44) + C.reset;
    console.log('\n' + rule);
    console.log('  ' + C.yellow + C.bold + 'JS' + C.reset + C.gray + '  ·  ' + C.reset
      + C.bold + 'Logged in as: ' + C.reset + active.name);
    console.log(rule);

    const options = [
      '[1] View all tasks',
      '[2] View my tasks',
      '[3] Add task',
      '[4] Edit task',
      '[5] Mark task complete',
      '[6] Delete task',
      '[7] Filter tasks',
      '[8] Concurrency demo',
      '[9] Switch user',
      '[0] Exit',
    ];
    for (const opt of options) {
      console.log('   ' + C.cyan + C.bold + opt.slice(0, 3) + C.reset + opt.slice(3));
      await sleep(30);
    }
    console.log(rule);

    const choice = (await ask('  ➤ ')).trim();

    if (['1', '2', '3', '4', '5', '6', '7', '8', '9'].includes(choice)) {
      await sweep();
    }

    switch (choice) {
      case '1': await viewTasksScreen(taskManager.getAllTasks()); break;
      case '2': await viewTasksScreen(
        taskManager.getAllTasks().filter(t => t.assignedTo === active.id)
      ); break;
      case '3': await addTaskScreen(); break;
      case '4': await editTaskScreen(); break;
      case '5': await markCompleteScreen(); break;
      case '6': await deleteTaskScreen(); break;
      case '7': await filterScreen(); break;
      case '8': await concurrencyDemoScreen(); break;
      case '9': await userSelectionScreen(); break;
      case '0': shutdown(); break;
    }
  }
}

async function start() {
  await userSelectionScreen();
  await mainMenu();
}

module.exports = { start };
