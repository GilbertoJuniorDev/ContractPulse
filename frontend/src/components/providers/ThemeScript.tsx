/**
 * Script inline que roda antes da hidratação do React para evitar flash
 * de tema errado (FOUC). Server Component puro — sem "use client".
 */
export default function ThemeScript() {
  const script = `
    (function() {
      try {
        var theme = localStorage.getItem('contractpulse-theme');
        if (!theme) {
          theme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        }
        if (theme === 'dark') {
          document.documentElement.classList.add('dark');
        } else {
          document.documentElement.classList.remove('dark');
        }
      } catch (e) {}
    })();
  `

  return <script dangerouslySetInnerHTML={{ __html: script }} />
}
