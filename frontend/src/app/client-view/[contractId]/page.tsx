'use client'

import { useState } from 'react'
import { useContract } from '@/hooks/useContracts'
import {
  useClientDashboard,
  useApproveTimeEntry,
  useDisputeTimeEntry,
} from '@/hooks/useTimeEntries'
import BurnRateChart from '@/components/charts/BurnRateChart'
import TimeEntryList from '@/components/time-entry/TimeEntryList'
import DisputeModal from '@/components/time-entry/DisputeModal'

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
  const approveMutation = useApproveTimeEntry()
  const [disputingEntryId, setDisputingEntryId] = useState<string | null>(null)

  const isLoading = isLoadingContract || isLoadingDashboard

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

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-5xl mx-auto px-4 py-4 sm:px-6 lg:px-8">
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
                Lançamentos Pendentes
              </h3>
              <p className="mt-2 text-3xl font-bold text-yellow-600">
                {dashboard.pendingEntries}
              </p>
              <p className="mt-1 text-xs text-gray-400">
                Aguardando aprovação
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
          <h2 className="mb-4 text-lg font-semibold text-gray-900">
            Lançamentos de Horas
          </h2>
          <TimeEntryList
            contractId={contractId}
            showActions
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
