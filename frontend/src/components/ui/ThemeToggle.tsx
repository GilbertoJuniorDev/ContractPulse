'use client'

import { useTheme } from '@/hooks/useTheme'

/**
 * Botão de alternância entre tema claro e escuro.
 * Exibe ícone de sol/lua com animação suave.
 */
export default function ThemeToggle() {
  const { isDark, toggleTheme, isMounted } = useTheme()

  if (!isMounted) {
    return (
      <div className="h-9 w-9 rounded-lg bg-gray-200 dark:bg-dark-card animate-pulse" />
    )
  }

  return (
    <button
      onClick={toggleTheme}
      className="relative inline-flex h-9 w-9 items-center justify-center rounded-lg
        border border-gray-200 bg-white text-gray-600 shadow-sm
        transition-all duration-300
        hover:bg-gray-50 hover:shadow-md
        dark:border-dark-border dark:bg-dark-card dark:text-gray-300
        dark:hover:bg-dark-hover dark:hover:shadow-glow"
      aria-label={isDark ? 'Ativar modo claro' : 'Ativar modo escuro'}
      title={isDark ? 'Modo claro' : 'Modo escuro'}
    >
      {/* Sol */}
      <svg
        className={`h-5 w-5 transition-all duration-300 absolute ${
          isDark
            ? 'rotate-90 scale-0 opacity-0'
            : 'rotate-0 scale-100 opacity-100'
        }`}
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
        />
      </svg>

      {/* Lua */}
      <svg
        className={`h-5 w-5 transition-all duration-300 absolute ${
          isDark
            ? 'rotate-0 scale-100 opacity-100'
            : '-rotate-90 scale-0 opacity-0'
        }`}
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
        />
      </svg>
    </button>
  )
}
