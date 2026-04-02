'use client'

import { useState, useRef, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Image from 'next/image'
import { User, LogOut, Settings } from 'lucide-react'
import { createClient } from '@/lib/supabase/client'

interface UserDropdownProps {
  email: string
  fullName?: string | null
  avatarUrl?: string | null
}

/**
 * Dropdown do usuário no header — exibe ícone/avatar e menu com perfil e logout.
 */
export default function UserDropdown({ email, fullName, avatarUrl }: UserDropdownProps) {
  const [isOpen, setIsOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)
  const router = useRouter()

  // Fecha o dropdown ao clicar fora
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  async function handleLogout() {
    const supabase = createClient()
    await supabase.auth.signOut()
    router.push('/login')
    router.refresh()
  }

  const displayInitial = (fullName ?? email).charAt(0).toUpperCase()

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Botão trigger */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 rounded-full p-1 hover:bg-gray-100 dark:hover:bg-dark-hover transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:focus:ring-offset-dark-bg"
        aria-expanded={isOpen}
        aria-haspopup="true"
      >
        <div className="h-8 w-8 shrink-0 overflow-hidden rounded-full bg-blue-100 dark:bg-blue-500/20">
          {avatarUrl ? (
            <Image
              src={avatarUrl}
              alt={fullName ?? email}
              width={32}
              height={32}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-sm font-semibold text-blue-600 dark:text-blue-400">
              {displayInitial}
            </div>
          )}
        </div>
      </button>

      {/* Menu dropdown */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-64 origin-top-right rounded-lg border border-gray-200 bg-white py-1 shadow-lg ring-1 ring-black/5 z-50 dark:border-dark-border dark:bg-dark-card dark:ring-white/5">
          {/* Info do usuário */}
          <div className="border-b border-gray-100 px-4 py-3 dark:border-dark-border">
            {fullName && (
              <p className="text-sm font-medium text-gray-900 truncate dark:text-white">{fullName}</p>
            )}
            <p className="text-sm text-gray-500 truncate dark:text-gray-400">{email}</p>
          </div>

          {/* Links */}
          <div className="py-1">
            <a
              href="/settings/profile"
              onClick={() => setIsOpen(false)}
              className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors dark:text-gray-300 dark:hover:bg-dark-hover"
            >
              <User className="h-4 w-4 text-gray-400 dark:text-gray-500" />
              Meu Perfil
            </a>
            <a
              href="/settings"
              onClick={() => setIsOpen(false)}
              className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors dark:text-gray-300 dark:hover:bg-dark-hover"
            >
              <Settings className="h-4 w-4 text-gray-400 dark:text-gray-500" />
              Configurações
            </a>
          </div>

          {/* Logout */}
          <div className="border-t border-gray-100 py-1 dark:border-dark-border">
            <button
              onClick={handleLogout}
              className="flex w-full items-center gap-3 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors dark:text-red-400 dark:hover:bg-red-500/10"
            >
              <LogOut className="h-4 w-4" />
              Sair
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
