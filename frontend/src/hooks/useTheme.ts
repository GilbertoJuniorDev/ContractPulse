'use client'

import { useCallback, useEffect, useState } from 'react'

type Theme = 'light' | 'dark'

const THEME_KEY = 'contractpulse-theme'

/**
 * Hook para controlar o tema (light/dark) da aplicação.
 * Persiste a preferência no localStorage e sincroniza a classe no <html>.
 */
export function useTheme() {
  const [theme, setThemeState] = useState<Theme>('light')
  const [isMounted, setIsMounted] = useState(false)

  useEffect(() => {
    setIsMounted(true)
    const stored = localStorage.getItem(THEME_KEY) as Theme | null
    const prefersDark = window.matchMedia(
      '(prefers-color-scheme: dark)'
    ).matches
    const initial = stored ?? (prefersDark ? 'dark' : 'light')
    setThemeState(initial)
    document.documentElement.classList.toggle('dark', initial === 'dark')
  }, [])

  const setTheme = useCallback((newTheme: Theme) => {
    setThemeState(newTheme)
    localStorage.setItem(THEME_KEY, newTheme)
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
  }, [])

  const toggleTheme = useCallback(() => {
    setTheme(theme === 'light' ? 'dark' : 'light')
  }, [theme, setTheme])

  const isDark = theme === 'dark'

  return { theme, isDark, setTheme, toggleTheme, isMounted }
}
