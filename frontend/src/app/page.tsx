import { redirect } from 'next/navigation'
import { createClient } from '@/lib/supabase/server'

/**
 * Página raiz — redireciona para /overview se autenticado, /login caso contrário.
 */
export default async function HomePage() {
  const supabase = createClient()
  const {
    data: { user },
  } = await supabase.auth.getUser()

  if (user) {
    redirect('/overview')
  } else {
    redirect('/login')
  }
}
