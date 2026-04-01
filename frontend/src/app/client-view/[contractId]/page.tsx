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
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <span className="ml-3 text-sm text-gray-600">Carregando dashboard...</span>
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
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-5xl mx-auto px-4 py-4 sm:px-6 lg:px-8">
          <Link
            href="/my-contracts"
            className="inline-flex items-center text-sm font-medium text-blue-600 hover:text-blue-800 mb-2"
          >
            &larr; Voltar para Meus Contratos
          </Link>
          <h1 className="text-xl font-bold text-gray-900">
            {contract?.title ?? 'Dashboard do Cliente'}
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            Acompanhe suas horas e gerencie lançamentos
          </p>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8 space-y-6">
        {/* Métricas */}
        {dashboard && (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
            <BurnRateChart dashboard={dashboard} />

            <div className="rounded-lg border border-gray-200 bg-white p-6">
              <h3 className="text-sm font-medium text-gray-500">
                Aguardando Revisão
              </h3>
              <p className="mt-2 text-3xl font-bold text-yellow-600">
                {reviewableCount}
              </p>
              <p className="mt-1 text-xs text-gray-400">
                Lançamentos para aprovar ou disputar
              </p>
            </div>

            <div className="rounded-lg border border-gray-200 bg-white p-6">
              <h3 className="text-sm font-medium text-gray-500">
                Aprovados / Disputados
              </h3>
              <div className="mt-2 flex items-end gap-2">
                <span className="text-3xl font-bold text-green-600">
                  {dashboard.approvedEntries}
                </span>
                <span className="text-lg text-gray-400">/</span>
                <span className="text-3xl font-bold text-red-600">
                  {dashboard.disputedEntries}
                </span>
              </div>
              <p className="mt-1 text-xs text-gray-400">
                Neste período de cobrança
              </p>
            </div>
          </div>
        )}

        {/* Lista de horas com ações de aprovação */}
        <div className="rounded-lg border border-gray-200 bg-white p-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">
              Lançamentos de Horas
            </h2>

            {hasPendingEntries && (
              <button
                type="button"
                onClick={handleBatchApprove}
                disabled={batchApproveMutation.isPending}
                className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:opacity-50"
              >
                {batchApproveMutation.isPending
                  ? 'Aprovando...'
                  : `Aprovar Todos (${reviewableCount})`}
              </button>
            )}
          </div>

          {batchApproveMutation.isSuccess && (
            <div className="mb-4 rounded-md border border-green-200 bg-green-50 p-3">
              <p className="text-sm text-green-700">
                Todos os lançamentos pendentes foram aprovados com sucesso.
              </p>
            </div>
          )}

          {batchApproveMutation.isError && (
            <div className="mb-4 rounded-md border border-red-200 bg-red-50 p-3">
              <p className="text-sm text-red-700">
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
