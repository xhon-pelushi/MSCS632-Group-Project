let _nextId = 1;
const users = [];
let activeUser = null;

function createUser(name) {
  const user = {
    id: String(_nextId++),
    name: name.trim(),
    createdAt: new Date().toISOString(),
  };
  users.push(user);
  return user;
}

function getUsers() {
  return [...users];
}

function getUserById(id) {
  return users.find(u => u.id === id) || null;
}

function setActiveUser(id) {
  const user = getUserById(id);
  if (!user) throw new Error(`User ${id} not found`);
  activeUser = user;
  return user;
}

function getActiveUser() {
  return activeUser;
}

// Pre-seed team members
createUser('Xhon Pelushi');
createUser('Sikun Peng');

module.exports = { createUser, getUsers, getUserById, setActiveUser, getActiveUser };
