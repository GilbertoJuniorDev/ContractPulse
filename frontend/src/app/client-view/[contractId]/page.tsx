'use client'

import { useState } from 'react'
import Link from 'next/link'
import { useContract } from '@/hooks/useContracts'
import {
  useClientDashboard,
  useTimeEntriesByContract,
  useApproveTimeEntry,
  useDisputeTimeEntry,
  useBatchApproveTimeEntries,
} from '@/hooks/useTimeEntries'
import BurnRateChart from '@/components/charts/BurnRateChart'
import TimeEntryList from '@/components/time-entry/TimeEntryList'
import DisputeModal from '@/components/time-entry/DisputeModal'
import ThemeToggle from '@/components/ui/ThemeToggle'
import type { TimeEntryStatus } from '@/lib/types/time-entry'

/** Status que o client pode revisar. */
const REVIEWABLE_STATUSES: TimeEntryStatus[] = ['SUBMITTED', 'PENDING_APPROVAL']

/**
 * Página de dashboard do cliente para um contrato específico.
 * View-only com ações de aprovação e disputa de lançamentos de horas.
 */
export default function ClientViewPage({
  params,
}: {
  params: { contractId: string }
}) {
  const contractId = params.contractId
  const { data: contract, isLoading: isLoadingContract } =
    useContract(contractId)
  const { data: dashboard, isLoading: isLoadingDashboard } =
    useClientDashboard(contractId)
  const { data: entries } = useTimeEntriesByContract(contractId)
  const approveMutation = useApproveTimeEntry()
  const batchApproveMutation = useBatchApproveTimeEntries()
  const [disputingEntryId, setDisputingEntryId] = useState<string | null>(null)

  const isLoading = isLoadingContract || isLoadingDashboard

  // Conta lançamentos revisáveis (SUBMITTED + PENDING_APPROVAL)
  const reviewableCount =
    entries?.filter((e) =>
      REVIEWABLE_STATUSES.includes(e.status as TimeEntryStatus)
    ).length ?? 0

  const hasPendingEntries = reviewableCount > 0

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-dark-bg">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <span className="ml-3 text-sm text-gray-600 dark:text-gray-400">Carregando dashboard...</span>
      </div>
    )
  }

  function handleApprove(timeEntryId: string) {
    approveMutation.mutate(timeEntryId)
  }

  function handleDispute(timeEntryId: string) {
    setDisputingEntryId(timeEntryId)
  }

  function handleBatchApprove() {
    batchApproveMutation.mutate(contractId)
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-dark-bg">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 dark:bg-dark-card dark:border-dark-border">
        <div className="max-w-5xl mx-auto px-4 py-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-2">
            <Link
              href="/my-contracts"
              className="inline-flex items-center text-sm font-medium text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
            >
              &larr; Voltar para Meus Contratos
            </Link>
            <ThemeToggle />
          </div>
          <h1 className="text-xl font-bold text-gray-900 dark:text-white">
            {contract?.title ?? 'Dashboard do Cliente'}
          </h1>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Acompanhe suas horas e gerencie lançamentos
          </p>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8 space-y-6">
        {/* Métricas */}
        {dashboard && (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
            <BurnRateChart dashboard={dashboard} />

            <div className="card">
              <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">
                Aguardando Revisão
              </h3>
              <p className="mt-2 text-3xl font-bold text-yellow-600 dark:text-amber-400">
                {reviewableCount}
              </p>
              <p className="mt-1 text-xs text-gray-400 dark:text-gray-500">
                Lançamentos para aprovar ou disputar
              </p>
            </div>

            <div className="card">
              <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">
                Aprovados / Disputados
              </h3>
              <div className="mt-2 flex items-end gap-2">
                <span className="text-3xl font-bold text-green-600 dark:text-green-400">
                  {dashboard.approvedEntries}
                </span>
                <span className="text-lg text-gray-400 dark:text-gray-500">/</span>
                <span className="text-3xl font-bold text-red-600 dark:text-red-400">
                  {dashboard.disputedEntries}
                </span>
              </div>
              <p className="mt-1 text-xs text-gray-400 dark:text-gray-500">
                Neste período de cobrança
              </p>
            </div>
          </div>
        )}

        {/* Lista de horas com ações de aprovação */}
        <div className="card">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
              Lançamentos de Horas
            </h2>

            {hasPendingEntries && (
              <button
                type="button"
                onClick={handleBatchApprove}
                disabled={batchApproveMutation.isPending}
                className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:opacity-50 dark:focus:ring-offset-dark-bg"
              >
                {batchApproveMutation.isPending
                  ? 'Aprovando...'
                  : `Aprovar Todos (${reviewableCount})`}
              </button>
            )}
          </div>

          {batchApproveMutation.isSuccess && (
            <div className="alert-success">
              <p className="text-sm text-green-700 dark:text-green-300">
                Todos os lançamentos pendentes foram aprovados com sucesso.
              </p>
            </div>
          )}

          {batchApproveMutation.isError && (
            <div className="alert-error">
              <p className="text-sm text-red-700 dark:text-red-300">
                Erro ao aprovar em lote. Tente novamente.
              </p>
            </div>
          )}

          <TimeEntryList
            contractId={contractId}
            showActions
            role="client"
            onApprove={handleApprove}
            onDispute={handleDispute}
          />
        </div>
      </main>

      {/* Modal de disputa */}
      {disputingEntryId && (
        <DisputeModal
          timeEntryId={disputingEntryId}
          onClose={() => setDisputingEntryId(null)}
        />
      )}
    </div>
  )
}
