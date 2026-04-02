import { redirect } from 'next/navigation'
import { createClient } from '@/lib/supabase/server'
import AuthSyncProvider from '@/components/providers/AuthSyncProvider'
import DashboardNav from '@/components/layout/DashboardNav'

export default async function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const supabase = createClient()
  const {
    data: { user },
  } = await supabase.auth.getUser()

  if (!user) {
    redirect('/login')
  }

  return (
    <div className="min-h-screen bg-gray-50 transition-colors duration-300 dark:bg-dark-bg">
      {/* Header */}
      <DashboardNav
        fallbackEmail={user.email ?? ''}
        fallbackFullName={user.user_metadata?.full_name ?? null}
      />

      {/* Conteúdo principal */}
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <AuthSyncProvider>{children}</AuthSyncProvider>
      </main>
    </div>
  )
}
