'use client'

import { useRouter } from 'next/navigation'
import { useProfile } from '@/hooks/useProfile'
import { useContractsByClient } from '@/hooks/useContracts'
import type { Contract } from '@/lib/types/contract'

const statusLabels: Record<string, { label: string; className: string }> = {
  ACTIVE: { label: 'Ativo', className: 'bg-green-100 text-green-800 dark:bg-green-500/20 dark:text-green-400' },
  PAUSED: { label: 'Pausado', className: 'bg-yellow-100 text-yellow-800 dark:bg-amber-500/20 dark:text-amber-400' },
  TERMINATED: { label: 'Encerrado', className: 'bg-gray-100 text-gray-600 dark:bg-gray-500/20 dark:text-gray-400' },
}

/**
 * Página que lista todos os contratos onde o usuário autenticado é o cliente.
 * O card de cada contrato redireciona para o client-view com dashboard e ações.
 */
export default function MyContractsPage() {
  const router = useRouter()
  const { user, isLoading: isLoadingProfile } = useProfile()
  const {
    data: contracts,
    isLoading: isLoadingContracts,
    error,
  } = useContractsByClient(user?.id ?? '')

  const isLoading = isLoadingProfile || isLoadingContracts

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <span className="ml-3 text-sm text-gray-600 dark:text-gray-400">
          Carregando seus contratos...
        </span>
      </div>
    )
  }

  if (error) {
    return (
      <div className="alert-error">
        Erro ao carregar contratos: {error.message}
      </div>
    )
  }

  if (!contracts || contracts.length === 0) {
    return (
      <div className="rounded-lg border-2 border-dashed border-gray-300 p-12 text-center dark:border-dark-border">
        <svg
          className="mx-auto h-12 w-12 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
        <h3 className="mt-4 text-sm font-medium text-gray-900 dark:text-white">
          Nenhum contrato encontrado
        </h3>
        <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
          Você ainda não é cliente em nenhum contrato.
        </p>
      </div>
    )
  }

  function handleContractClick(contract: Contract) {
    router.push(`/client-view/${contract.id}`)
  }

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
          Meus Contratos
        </h2>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Contratos onde você é o cliente. Clique para ver detalhes e gerenciar horas.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {contracts.map((contract) => {
          const statusInfo = statusLabels[contract.status] ?? {
            label: contract.status,
            className: 'bg-gray-100 text-gray-600',
          }
          return (
            <button
              key={contract.id}
              onClick={() => handleContractClick(contract)}
              className="rounded-xl border border-gray-200 bg-white p-5 text-left shadow-sm transition-all hover:shadow-md dark:border-dark-border dark:bg-dark-card dark:hover:shadow-glow"
            >
              <div className="flex items-start justify-between">
                <h3 className="font-medium text-gray-900 dark:text-white">
                  {contract.title}
                </h3>
                <span
                  className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${statusInfo.className}`}
                >
                  {statusInfo.label}
                </span>
              </div>
              <p className="mt-2 text-xs text-gray-500 dark:text-gray-400">
                Tipo: {contract.type.replace('_', ' ')}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Início: {new Date(contract.startDate).toLocaleDateString('pt-BR')}
              </p>
              {contract.endDate && (
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Término: {new Date(contract.endDate).toLocaleDateString('pt-BR')}
                </p>
              )}
            </button>
          )
        })}
      </div>
    </div>
  )
}
