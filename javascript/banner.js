// Animated startup splash. Reveals a "JS / JavaScript" banner line-by-line and
// then runs a short rotating spinner before the main menu appears.

const RESET  = '\x1b[0m';
const BOLD   = '\x1b[1m';
const YELLOW = '\x1b[38;5;226m';
const CYAN   = '\x1b[36m';
const GREEN  = '\x1b[32m';

const JS_ART = [
  '     ██   ██████',
  '     ██  ██     ',
  '     ██   █████ ',
  '██   ██       ██',
  ' █████   ██████ ',
];

const SPINNER = ['|', '/', '-', '\\'];

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

async function show() {
  process.stdout.write('\x1b[2J\x1b[H');
  console.log();
  for (const line of JS_ART) {
    console.log(BOLD + YELLOW + line + RESET);
    await sleep(70);
  }
  console.log('\n' + BOLD + YELLOW + '       JavaScript' + RESET + '\n');

  const label = CYAN + '  Spinning up the event loop ' + RESET;
  for (let i = 0; i < 16; i++) {
    process.stdout.write('\r' + label + YELLOW + SPINNER[i % SPINNER.length] + RESET);
    await sleep(80);
  }
  process.stdout.write('\r' + label + GREEN + 'done' + RESET + '\n');
  await sleep(150);
}

module.exports = { show };
