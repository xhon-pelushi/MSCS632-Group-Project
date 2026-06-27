const { start } = require('./menu');

start().catch(err => {
  console.error('Fatal error:', err.message);
  process.exit(1);
});
