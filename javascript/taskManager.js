const { STATUS } = require('./constants');

// Async mutex — serializes concurrent writes the way synchronized blocks do in Java.
// JavaScript's event loop prevents true data races, but without a mutex two async
// callers can both read stale state before either write completes.
class AsyncMutex {
  constructor() {
    this._locked = false;
    this._queue = [];
  }

  lock() {
    return new Promise(resolve => {
      if (!this._locked) {
        this._locked = true;
        resolve();
      } else {
        this._queue.push(resolve);
      }
    });
  }

  unlock() {
    if (this._queue.length > 0) {
      this._queue.shift()();
    } else {
      this._locked = false;
    }
  }
}

const mutex = new AsyncMutex();
const tasks = [];
let _nextId = 1;

async function addTask({ title, description = '', category, assignedTo, createdBy }) {
  await mutex.lock();
  try {
    const task = {
      id: String(_nextId++),
      title: title.trim(),
      description: description.trim(),
      category,
      status: STATUS.PENDING,
      assignedTo,
      createdBy,
      createdAt: new Date().toISOString(),
    };
    tasks.push(task);
    return task;
  } finally {
    mutex.unlock();
  }
}

async function updateTask(id, updates) {
  await mutex.lock();
  try {
    const task = tasks.find(t => t.id === id);
    if (!task) throw new Error(`Task ${id} not found`);
    Object.assign(task, updates);
    return { ...task };
  } finally {
    mutex.unlock();
  }
}

async function deleteTask(id) {
  await mutex.lock();
  try {
    const index = tasks.findIndex(t => t.id === id);
    if (index === -1) throw new Error(`Task ${id} not found`);
    tasks.splice(index, 1);
  } finally {
    mutex.unlock();
  }
}

function getAllTasks() {
  return [...tasks];
}

function getTaskById(id) {
  return tasks.find(t => t.id === id) || null;
}

function filterByCategory(category) {
  return tasks.filter(t => t.category === category);
}

function filterByStatus(status) {
  return tasks.filter(t => t.status === status);
}

// Launches three overlapping async operations via Promise.all to demonstrate
// JavaScript's concurrency model. The mutex ensures each write is atomic even
// though all three are in-flight simultaneously.
async function simulateConcurrentAccess() {
  if (tasks.length < 2) {
    return ['  (Add at least 2 tasks first to run this demo)'];
  }

  const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
  const log = [];

  async function userAction(userName, taskId, newStatus, delayMs) {
    await delay(delayMs);
    log.push(`  ${userName} is requesting lock to update task [${taskId}]...`);
    await mutex.lock();
    try {
      await delay(80); // simulate processing time while holding the lock
      const task = tasks.find(t => t.id === taskId);
      if (task) {
        task.status = newStatus;
        log.push(`  ${userName} updated task "${task.title}" → ${newStatus}`);
      }
    } finally {
      mutex.unlock();
      log.push(`  ${userName} released lock`);
    }
  }

  // All three fire at nearly the same time — mutex queues them safely
  await Promise.all([
    userAction('Xhon',  tasks[0].id, STATUS.IN_PROGRESS, 0),
    userAction('Sikun', tasks[1].id, STATUS.COMPLETED,   10),
    userAction('Xhon',  tasks[1].id, STATUS.IN_PROGRESS, 20),
  ]);

  return log;
}

module.exports = {
  addTask, updateTask, deleteTask,
  getAllTasks, getTaskById,
  filterByCategory, filterByStatus,
  simulateConcurrentAccess,
};
