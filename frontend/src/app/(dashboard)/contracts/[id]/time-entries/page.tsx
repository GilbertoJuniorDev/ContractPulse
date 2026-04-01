'use client'

import { useState } from 'react'
import { useContract } from '@/hooks/useContracts'
import { useSubmitTimeEntry, useDeleteTimeEntry } from '@/hooks/useTimeEntries'
import TimeEntryForm from '@/components/time-entry/TimeEntryForm'
import TimeEntryList from '@/components/time-entry/TimeEntryList'

/**
 * Página de lançamentos de horas de um contrato (visão do provider).
 * Permite criar novos lançamentos e visualizar o histórico.
 */
export default function TimeEntriesPage({
  params,
}: {
  params: { id: string }
}) {
  const contractId = params.id
  const { data: contract, isLoading } = useContract(contractId)
  const [showForm, setShowForm] = useState(false)
  const submitMutation = useSubmitTimeEntry()
  const deleteMutation = useDeleteTimeEntry()

  function handleSubmitEntry(timeEntryId: string) {
    submitMutation.mutate(timeEntryId)
  }

  function handleDeleteEntry(timeEntryId: string) {
    if (window.confirm('Tem certeza que deseja remover este lançamento?')) {
      deleteMutation.mutate(timeEntryId)
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <span className="ml-3 text-sm text-gray-600">Carregando...</span>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Lançamentos de Horas
          </h1>
          {contract && (
            <p className="mt-1 text-sm text-gray-500">
              Contrato: {contract.title}
            </p>
          )}
        </div>

        {contract?.status === 'ACTIVE' && (
          <button
            onClick={() => setShowForm((prev) => !prev)}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            {showForm ? 'Fechar Formulário' : 'Novo Lançamento'}
          </button>
        )}
      </div>

      {/* Formulário de criação */}
      {showForm && contract?.status === 'ACTIVE' && (
        <div className="rounded-lg border border-gray-200 bg-white p-6">
          <h2 className="mb-4 text-lg font-semibold text-gray-900">
            Novo Lançamento
          </h2>
          <TimeEntryForm
            contractId={contractId}
            onSuccess={() => setShowForm(false)}
            onCancel={() => setShowForm(false)}
          />
        </div>
      )}

      {/* Lista de lançamentos */}
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          Histórico de Lançamentos
        </h2>
        <TimeEntryList
          contractId={contractId}
          showActions
          role="provider"
          onSubmit={handleSubmitEntry}
          onDelete={handleDeleteEntry}
        />
      </div>
    </div>
  )
}
