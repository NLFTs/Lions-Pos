const fs = require('fs');
let c = fs.readFileSync('src/stores/auth.js', 'utf8');

c = c.replace(/function loadJson[\s\S]*?catch \{\s*return fallback\s*\}/, `function loadJson(key, fallback) {
  if (typeof window === 'undefined') return fallback;
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function loadString(key, fallback) {
  if (typeof window === 'undefined') return fallback;
  try {
    return localStorage.getItem(key) || fallback;
  } catch {
    return fallback
  }
}`);

fs.writeFileSync('src/stores/auth.js', c);
