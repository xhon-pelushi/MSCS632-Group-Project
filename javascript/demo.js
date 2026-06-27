/**
 * demo.js — produces focused, screenshot-friendly output for each feature.
 * Run with: node demo.js <scene>
 * Scenes: users | menu | add | tasks | filter | concurrency
 */
const userManager = require('./userManager');
const taskManager = require('./taskManager');
const { CATEGORY, STATUS } = require('./constants');

const hr = () => console.log('─'.repeat(52));

function header(title) {
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

async function seedTasks() {
  const xhon  = userManager.getUsers()[0];
  const sikun = userManager.getUsers()[1];
  await taskManager.addTask({ title: 'Fix login bug',       description: 'Users cannot log in after password reset', category: CATEGORY.WORK,     assignedTo: xhon.id,  createdBy: xhon.id  });
  await taskManager.addTask({ title: 'Write unit tests',    description: 'Cover TaskManager and UserManager',       category: CATEGORY.WORK,     assignedTo: sikun.id, createdBy: xhon.id  });
  await taskManager.addTask({ title: 'Buy groceries',       description: '',                                        category: CATEGORY.SHOPPING, assignedTo: xhon.id,  createdBy: xhon.id  });
  await taskManager.addTask({ title: 'Update README',       description: 'Add build and run instructions',          category: CATEGORY.WORK,     assignedTo: sikun.id, createdBy: sikun.id });
  await taskManager.addTask({ title: 'Review pull request', description: 'Check Sikun\'s Java implementation',      category: CATEGORY.WORK,     assignedTo: xhon.id,  createdBy: sikun.id });
}

const scene = process.argv[2];

(async () => {
  switch (scene) {

    case 'users': {
      header('Collaborative To-Do List');
      console.log('\n  Select a user:\n');
      const users = userManager.getUsers();
      users.forEach((u, i) => console.log(`    ${i + 1}. ${u.name}`));
      console.log(`    ${users.length + 1}. Add new user`);
      console.log('    0. Exit\n');
      console.log('  > _');
      break;
    }

    case 'menu': {
      const active = userManager.getUsers()[0];
      header(`Logged in as: ${active.name}`);
      console.log('\n  1. View all tasks');
      console.log('  2. View my tasks');
      console.log('  3. Add task');
      console.log('  4. Edit task');
      console.log('  5. Mark task complete');
      console.log('  6. Delete task');
      console.log('  7. Filter tasks');
      console.log('  8. Concurrency demo');
      console.log('  9. Switch user');
      console.log('  0. Exit\n');
      console.log('  > _');
      break;
    }

    case 'add': {
      header('Add New Task');
      console.log('\n  Title:   Fix login bug');
      console.log('  Description (optional):  Users cannot log in after password reset');
      console.log('\n  Category:');
      console.log('    1. WORK');
      console.log('    2. PERSONAL');
      console.log('    3. SHOPPING');
      console.log('    4. OTHER');
      console.log('  > 1');
      console.log('\n  Assign to:');
      userManager.getUsers().forEach((u, i) => console.log(`    ${i + 1}. ${u.name}`));
      console.log('  > 1');
      console.log('\n  ✓ Task "Fix login bug" added.');
      break;
    }

    case 'tasks': {
      await seedTasks();
      // mark one complete and one in progress for variety
      const all = taskManager.getAllTasks();
      await taskManager.updateTask(all[1].id, { status: STATUS.COMPLETED });
      await taskManager.updateTask(all[3].id, { status: STATUS.IN_PROGRESS });
      header('All Tasks');
      console.log();
      taskManager.getAllTasks().forEach(t => console.log(taskLine(t) + '\n'));
      break;
    }

    case 'filter': {
      await seedTasks();
      await taskManager.updateTask(taskManager.getAllTasks()[0].id, { status: STATUS.COMPLETED });
      await taskManager.updateTask(taskManager.getAllTasks()[2].id, { status: STATUS.COMPLETED });
      header('Filter → Status: COMPLETED');
      const completed = taskManager.filterByStatus(STATUS.COMPLETED);
      console.log();
      if (completed.length === 0) {
        console.log('  No tasks found.');
      } else {
        completed.forEach(t => console.log(taskLine(t) + '\n'));
      }
      break;
    }

    case 'concurrency': {
      await seedTasks();
      header('Concurrency Demo');
      console.log('\n  Simulating concurrent task updates with async/await + mutex...\n');
      const log = await taskManager.simulateConcurrentAccess();
      log.forEach(l => console.log(l));
      console.log('\n  Promise.all fires all operations simultaneously.');
      console.log('  The AsyncMutex queues writers so each update is atomic,');
      console.log("  mirroring Java's synchronized blocks on a shared list.");
      break;
    }

    default:
      console.error('Usage: node demo.js <users|menu|add|tasks|filter|concurrency>');
      process.exit(1);
  }
})();
