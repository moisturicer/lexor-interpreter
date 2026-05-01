// LEXOR language definition for Monaco Editor (Monarch tokenizer)
window.LEXOR_LANGUAGE = {
  id: 'lexor',

  monarchTokens: {
    keywords: [
      'SCRIPT', 'AREA', 'START', 'END',
      'DECLARE', 'PRINT', 'SCAN',
      'IF', 'ELSE', 'FOR', 'REPEAT', 'WHEN',
      'AND', 'OR', 'NOT',
    ],
    typeKeywords: ['INT', 'FLOAT', 'CHAR', 'BOOL'],
    boolLiterals: ['TRUE', 'FALSE'],

    tokenizer: {
      root: [
        // comments
        [/%%.*$/, 'comment'],

        // keywords
        [/[A-Z_][A-Z0-9_]*/, {
          cases: {
            '@keywords':     'keyword',
            '@typeKeywords': 'type',
            '@boolLiterals': 'number',
            '@default':      'identifier',
          }
        }],

        // lowercase identifiers (variable names)
        [/[a-z_][a-zA-Z0-9_]*/, 'variable'],

        // numbers
        [/\d+\.\d+/, 'number.float'],
        [/\d+/,      'number'],

        // char literal
        [/'.'/, 'string'],

        // bool/string literal in double quotes
        [/"[^"]*"/, 'string'],

        // escape code [#] etc
        [/\[[^\]]*\]/, 'string.escape'],

        // special LEXOR symbols
        [/&/, 'delimiter'],
        [/\$/, 'delimiter'],

        // operators
        [/[+\-*\/%]/, 'operator'],
        [/<>|>=|<=|>|<|==/, 'operator'],
        [/=/, 'operator'],

        // punctuation
        [/[():,]/, 'delimiter'],

        // whitespace
        [/\s+/, 'white'],
      ],
    },
  },

  theme: {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'comment',       foreground: '555868', fontStyle: 'italic' },
      { token: 'keyword',       foreground: '00e5a0', fontStyle: 'bold' },
      { token: 'type',          foreground: '4da6ff' },
      { token: 'identifier',    foreground: 'e2e4ec' },
      { token: 'variable',      foreground: 'c9d1f5' },
      { token: 'number',        foreground: 'ffd166' },
      { token: 'number.float',  foreground: 'ffd166' },
      { token: 'string',        foreground: 'ff9f7f' },
      { token: 'string.escape', foreground: 'ff6b9d' },
      { token: 'operator',      foreground: '8b8fa8' },
      { token: 'delimiter',     foreground: '00e5a0' },
    ],
    colors: {
      'editor.background':          '#0d0e11',
      'editor.foreground':          '#e2e4ec',
      'editorLineNumber.foreground':'#383c4a',
      'editorLineNumber.activeForeground': '#8b8fa8',
      'editor.lineHighlightBackground':    '#13151a',
      'editorCursor.foreground':    '#00e5a0',
      'editor.selectionBackground': '#00e5a020',
      'editorIndentGuide.background':'#2a2d36',
    },
  },
};