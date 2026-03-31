'use client'

import { useEffect, useRef, type ReactNode } from 'react'
import { createClient } from '@/lib/supabase/client'
import { syncUser } from '@/lib/api/users'

/**
 * Provider que sincroniza automaticamente o usuário do Supabase Auth
 * com a tabela de usuários do backend Java.
 *
 * Deve envolver as páginas do dashboard para garantir que o usuário
 * local exista antes de qualquer operação no backend.
 */
export default function AuthSyncProvider({ children }: { children: ReactNode }) {
  const hasSynced = useRef(false)

  useEffect(() => {
    if (hasSynced.current) return

    async function performSync() {
      try {
        const supabase = createClient()
        const { data: { user } } = await supabase.auth.getUser()

        if (!user) return

        await syncUser({
          fullName: user.user_metadata?.full_name
            || user.user_metadata?.name
            || user.email?.split('@')[0]
            || 'Usuário',
          email: user.email || '',
          avatarUrl: user.user_metadata?.avatar_url || null,
        })

        hasSynced.current = true
      } catch (error) {
        // Silencia erros de sync — o usuário pode já existir ou o backend estar indisponível.
        // Em próximas operações, o erro aparecerá de forma mais específica.
        console.warn('Falha ao sincronizar usuário com backend:', error)
      }
    }

    performSync()
  }, [])

  return <>{children}</>
}
