'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import ThemeToggle from '@/components/ui/ThemeToggle'
import NotificationBell from '@/components/notifications/NotificationBell'
import HeaderUserDropdown from '@/components/auth/HeaderUserDropdown'

interface DashboardNavProps {
  fallbackEmail: string
  fallbackFullName?: string | null
}

const navLinks = [
  { href: '/overview', label: 'Visão Geral' },
  { href: '/contracts', label: 'Contratos' },
  { href: '/my-contracts', label: 'Meus Contratos' },
  { href: '/settings', label: 'Configurações' },
] as const

/**
 * Barra de navegação principal do dashboard.
 * Inclui toggle de tema, links de navegação e dropdown do usuário.
 */
export default function DashboardNav({
  fallbackEmail,
  fallbackFullName,
}: DashboardNavProps) {
  const pathname = usePathname()

  function isActive(href: string): boolean {
    return pathname.startsWith(href)
  }

  return (
    <header className="sticky top-0 z-40 border-b border-gray-200 bg-white/80 backdrop-blur-lg transition-colors duration-300 dark:border-dark-border dark:bg-dark-card/80">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link href="/overview" className="flex items-center gap-2.5">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-blue-500 to-blue-700 shadow-sm">
              <span className="text-sm font-bold text-white">CP</span>
            </div>
            <h1 className="text-lg font-bold tracking-tight text-gray-900 dark:text-white">
              ContractPulse
            </h1>
          </Link>

          {/* Nav links */}
          <nav className="hidden items-center gap-1 md:flex">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className={`rounded-lg px-3 py-2 text-sm font-medium transition-all duration-200 ${
                  isActive(link.href)
                    ? 'bg-blue-50 text-blue-700 dark:bg-blue-500/10 dark:text-blue-400'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 dark:text-gray-400 dark:hover:bg-dark-hover dark:hover:text-gray-200'
                }`}
              >
                {link.label}
              </Link>
            ))}
          </nav>

          {/* Actions */}
          <div className="flex items-center gap-3">
            <ThemeToggle />
            <NotificationBell />
            <HeaderUserDropdown
              fallbackEmail={fallbackEmail}
              fallbackFullName={fallbackFullName}
            />
          </div>
        </div>
      </div>
    </header>
  )
}
