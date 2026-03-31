import { redirect } from 'next/navigation'
import { createClient } from '@/lib/supabase/server'
import HeaderUserDropdown from '@/components/auth/HeaderUserDropdown'
import AuthSyncProvider from '@/components/providers/AuthSyncProvider'

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
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-gray-900">ContractPulse</h1>
            </div>

            <nav className="hidden md:flex items-center gap-6">
              <a
                href="/overview"
                className="text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
              >
                Visão Geral
              </a>
              <a
                href="/contracts"
                className="text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
              >
                Contratos
              </a>
              <a
                href="/my-contracts"
                className="text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
              >
                Meus Contratos
              </a>
              <a
                href="/settings"
                className="text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
              >
                Configurações
              </a>
            </nav>

            <div className="flex items-center">
              <HeaderUserDropdown
                fallbackEmail={user.email ?? ''}
                fallbackFullName={user.user_metadata?.full_name ?? null}
              />
            </div>
          </div>
        </div>
      </header>

      {/* Conteúdo principal */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <AuthSyncProvider>{children}</AuthSyncProvider>
      </main>
    </div>
  )
}
