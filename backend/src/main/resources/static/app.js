const BACKEND = '';

const EXAMPLES = {
  hello: `%% hello world
SCRIPT AREA
START SCRIPT
DECLARE INT x
x = 5
PRINT: "Hello, World!" & $ & "x is: " & x
END SCRIPT`,

  arithmetic: `%% arithmetic operations
SCRIPT AREA
START SCRIPT
DECLARE INT xyz, abc=100
xyz = ((abc * 5) / 10 + 10) * -1
PRINT: [[] & xyz & []]
END SCRIPT`,

  logical: `%% logical operations
SCRIPT AREA
START SCRIPT
DECLARE INT a=100, b=200, c=300
DECLARE BOOL d="FALSE"
d = (a < b AND c <> 200)
PRINT: d
END SCRIPT`,

  ifelse: `%% if / else example
SCRIPT AREA
START SCRIPT
DECLARE INT x=10, y=20
IF (x < y)
START IF
PRINT: "x is smaller" & $
END IF
ELSE
START IF
PRINT: "y is smaller" & $
END IF
END SCRIPT`,

  loop: `%% repeat when loop
SCRIPT AREA
START SCRIPT
DECLARE INT x=5
REPEAT WHEN (x > 0)
START REPEAT
PRINT: x & $
x = x - 1
END REPEAT
END SCRIPT`,
};

let editor;

require.config({
  paths: { vs: 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs' }
});

require(['vs/editor/editor.main'], () => {
  const lang  = window.LEXOR_LANGUAGE;

  // register language
  monaco.languages.register({ id: lang.id });
  monaco.languages.setMonarchTokensProvider(lang.id, lang.monarchTokens);
  monaco.editor.defineTheme('lexor-dark', lang.theme);

  // create editor
  editor = monaco.editor.create(document.getElementById('editor'), {
    value: EXAMPLES.hello,
    language: 'lexor',
    theme: 'lexor-dark',
    fontFamily: "'Space Mono', monospace",
    fontSize: 13,
    lineHeight: 22,
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    renderLineHighlight: 'all',
    cursorBlinking: 'smooth',
    cursorStyle: 'line',
    padding: { top: 16, bottom: 16 },
    automaticLayout: true,
    tabSize: 2,
    wordWrap: 'on',
  });

  // Ctrl+Enter to run
  editor.addCommand(
    monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter,
    () => runCode()
  );

  setStatus('ready');
});

// ── run ──────────────────────────────────────────────────────

async function runCode() {
  const code  = editor.getValue();
  const input = document.getElementById('input-area').value.trim();
  const btn   = document.getElementById('run-btn');
  const out   = document.getElementById('output-area');

  btn.classList.add('loading');
  btn.querySelector('span:last-child').textContent = 'Running…';
  setStatus('running…');
  out.className = '';
  out.textContent = '';

  try {
    const res = await fetch(`${BACKEND}/api/run`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, input }),
    });

    if (!res.ok) throw new Error(`HTTP ${res.status}`);

    const data = await res.json();

    if (data.error) {
      out.className = 'error';
      out.textContent = data.error;
      setStatus('error');
    } else {
      out.className = 'success';
      out.textContent = data.output || '(no output)';
      setStatus('done');
    }

  } catch (e) {
    out.className = 'error';
    out.textContent = `connection error: could not reach backend at ${BACKEND}\n\n${e.message}`;
    setStatus('error');
  } finally {
    btn.classList.remove('loading');
    btn.querySelector('span:last-child').textContent = 'Run';
  }
}

// ── examples ─────────────────────────────────────────────────

document.getElementById('example-select').addEventListener('change', function () {
  const key = this.value;
  if (key && EXAMPLES[key] && editor) {
    editor.setValue(EXAMPLES[key]);
    this.value = '';
    document.getElementById('output-area').innerHTML =
      '<span class="placeholder">// run your program to see output here</span>';
    setStatus('ready');
  }
});

// ── run button ───────────────────────────────────────────────

document.getElementById('run-btn').addEventListener('click', runCode);

// ── draggable divider ─────────────────────────────────────────

const divider    = document.getElementById('divider');
const paneEditor = document.querySelector('.pane-editor');
let dragging = false;

divider.addEventListener('mousedown', e => {
  dragging = true;
  divider.classList.add('dragging');
  e.preventDefault();
});

document.addEventListener('mousemove', e => {
  if (!dragging) return;
  const main = document.querySelector('main');
  const rect = main.getBoundingClientRect();
  let pct = ((e.clientX - rect.left) / rect.width) * 100;
  pct = Math.max(25, Math.min(75, pct));
  paneEditor.style.flex = `0 0 ${pct}%`;
});

document.addEventListener('mouseup', () => {
  if (dragging) {
    dragging = false;
    divider.classList.remove('dragging');
    editor && editor.layout();
  }
});

// ── status ────────────────────────────────────────────────────

function setStatus(msg) {
  document.getElementById('status-msg').textContent = msg;
}