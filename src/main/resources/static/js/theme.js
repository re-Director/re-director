function changeTheme(theme) {
  localStorage.setItem('theme', theme);
  applyTheme(theme);
}

function applyTheme(theme) {
  if (theme === 'auto') {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    document.documentElement.setAttribute('data-theme', prefersDark ? 'dark' : 'light');
  } else {
    document.documentElement.setAttribute('data-theme', theme);
  }
}

function loadTheme() {
    const savedTheme = localStorage.getItem('theme') || 'auto';
    const themeSelector = document.getElementById('theme-selector');
    if (themeSelector) {
      themeSelector.value = savedTheme;
    }
    applyTheme(savedTheme);
}

loadTheme();
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
  if (document.getElementById('theme-selector').value === 'auto') {
    applyTheme('auto');
  }
});