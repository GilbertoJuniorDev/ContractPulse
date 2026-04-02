'use client'

import { Suspense, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'

/**
 * Conteúdo interno do callback — usa useSearchParams e precisa de Suspense.
 */
function CallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const error = searchParams.get('error')

  useEffect(() => {
    if (error) {
      router.replace(`/login?error=${error}`)
    } else {
      router.replace('/overview')
    }
  }, [error, router])

  return (
    <div className="text-center">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 dark:border-blue-400 mx-auto" />
      <p className="mt-4 text-sm text-gray-600 dark:text-gray-400">Processando autenticação...</p>
    </div>
  )
}

/**
 * Página de fallback para o callback de autenticação.
 * O fluxo principal OAuth passa pelo route handler em /api/auth/callback.
 * Esta página exibe loading ou redireciona em caso de erro.
 */
export default function AuthCallbackPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-dark-bg">
      <Suspense
        fallback={
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 dark:border-blue-400 mx-auto" />
            <p className="mt-4 text-sm text-gray-600 dark:text-gray-400">Carregando...</p>
          </div>
        }
      >
        <CallbackContent />
      </Suspense>
    </div>
  )
}
