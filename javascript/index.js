const banner = require('./banner');
const { start } = require('./menu');

banner.show()
  .then(() => start())
  .catch(err => {
    console.error('Fatal error:', err.message);
    process.exit(1);
  });
