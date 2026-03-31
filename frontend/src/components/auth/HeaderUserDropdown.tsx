'use client'

import { useProfile } from '@/hooks/useProfile'
import UserDropdown from '@/components/auth/UserDropdown'

interface HeaderUserDropdownProps {
  /** Fallback do email (vindo do server component / Supabase Auth) */
  fallbackEmail: string
  fallbackFullName?: string | null
}

/**
 * Wrapper client que busca dados de perfil do backend para o UserDropdown.
 * Usa email/nome do Supabase Auth como fallback enquanto carrega.
 * O avatar NUNCA usa fallback do Google — sempre vem do backend.
 */
export default function HeaderUserDropdown({
  fallbackEmail,
  fallbackFullName,
}: HeaderUserDropdownProps) {
  const { user } = useProfile()

  return (
    <UserDropdown
      email={user?.email ?? fallbackEmail}
      fullName={user?.fullName ?? fallbackFullName}
      avatarUrl={user?.avatarUrl ?? null}
    />
  )
}
