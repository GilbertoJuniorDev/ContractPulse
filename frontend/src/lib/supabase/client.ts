import { createBrowserClient } from '@supabase/ssr'

/**
 * Cria um cliente Supabase para uso no browser (Client Components).
 * Usa as variáveis de ambiente públicas do Next.js.
 */
export function createClient() {
  return createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  )
}
