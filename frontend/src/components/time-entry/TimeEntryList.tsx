/**
 * Lista de lançamentos de horas com status e ações.
 *
 * Ciclo: DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 */
'use client'

import { useTimeEntriesByContract } from '@/hooks/useTimeEntries'
import ApprovalBadge from './ApprovalBadge'
import type { TimeEntryStatus } from '@/lib/types/time-entry'

interface TimeEntryListProps {
  contractId: string
  showActions?: boolean
  /** Define quem está visualizando — controla quais ações são exibidas. */
  role?: 'provider' | 'client'
  onSubmit?: (id: string) => void
  onDelete?: (id: string) => void
  onApprove?: (id: string) => void
  onDispute?: (id: string) => void
}

/** Status que o client pode revisar (aprovar ou disputar). */
const CLIENT_REVIEWABLE_STATUSES: TimeEntryStatus[] = [
  'SUBMITTED',
  'PENDING_APPROVAL',
]

/**
 * Lista de lançamentos de horas de um contrato.
 *
 * - Provider vê: Enviar e Remover (apenas DRAFT)
 * - Client vê: Aprovar e Disputar (SUBMITTED + PENDING_APPROVAL)
 */
export default function TimeEntryList({
  contractId,
  showActions = false,
  role = 'provider',
  onSubmit,
  onDelete,
  onApprove,
  onDispute,
}: TimeEntryListProps) {
  const { data: entries, isLoading, error } = useTimeEntriesByContract(contractId)

  if (isLoading) {
    return <p className="text-sm text-gray-500 dark:text-gray-400">Carregando lançamentos...</p>
  }

  if (error) {
    return (
      <p className="text-sm text-red-600 dark:text-red-400">
        Erro ao carregar lançamentos: {error.message}
      </p>
    )
  }

  if (!entries || entries.length === 0) {
    return (
      <p className="text-sm text-gray-500 dark:text-gray-400">Nenhum lançamento encontrado.</p>
    )
  }

  const renderActions = (entry: (typeof entries)[0]) => {
    if (!showActions) return null

    if (role === 'provider' && entry.status === 'DRAFT') {
      return (
        <td className="whitespace-nowrap px-4 py-3 text-center">
          <div className="flex justify-center gap-2">
            <button
              onClick={() => onSubmit?.(entry.id)}
              className="rounded bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 hover:bg-blue-100 dark:bg-blue-500/20 dark:text-blue-400 dark:hover:bg-blue-500/30"
            >
              Enviar
            </button>
            <button
              onClick={() => onDelete?.(entry.id)}
              className="rounded bg-red-50 px-3 py-1 text-xs font-medium text-red-700 hover:bg-red-100 dark:bg-red-500/20 dark:text-red-400 dark:hover:bg-red-500/30"
            >
              Remover
            </button>
          </div>
        </td>
      )
    }

    if (role === 'client' && CLIENT_REVIEWABLE_STATUSES.includes(entry.status as TimeEntryStatus)) {
      return (
        <td className="whitespace-nowrap px-4 py-3 text-center">
          <div className="flex justify-center gap-2">
            <button
              onClick={() => onApprove?.(entry.id)}
              className="rounded bg-green-50 px-3 py-1 text-xs font-medium text-green-700 hover:bg-green-100 dark:bg-green-500/20 dark:text-green-400 dark:hover:bg-green-500/30"
            >
              Aprovar
            </button>
            <button
              onClick={() => onDispute?.(entry.id)}
              className="rounded bg-red-50 px-3 py-1 text-xs font-medium text-red-700 hover:bg-red-100 dark:bg-red-500/20 dark:text-red-400 dark:hover:bg-red-500/30"
            >
              Disputar
            </button>
          </div>
        </td>
      )
    }

    return (
      <td className="whitespace-nowrap px-4 py-3 text-center text-xs text-gray-400 dark:text-gray-500">
        —
      </td>
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-dark-border">
        <thead className="bg-gray-50 dark:bg-dark-hover">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
              Data
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
              Descrição
            </th>
            <th className="px-4 py-3 text-right text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
              Horas
            </th>
            <th className="px-4 py-3 text-center text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
              Status
            </th>
            {showActions && (
              <th className="px-4 py-3 text-center text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
                Ações
              </th>
            )}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white dark:divide-dark-border dark:bg-dark-card">
          {entries.map((entry) => (
            <tr key={entry.id} className="hover:bg-gray-50 dark:hover:bg-dark-hover">
              <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900 dark:text-white">
                {new Date(entry.entryDate).toLocaleDateString('pt-BR')}
              </td>
              <td className="px-4 py-3 text-sm text-gray-700 dark:text-gray-300">
                {entry.description}
              </td>
              <td className="whitespace-nowrap px-4 py-3 text-right text-sm font-medium text-gray-900 dark:text-white">
                {entry.hours}h
              </td>
              <td className="whitespace-nowrap px-4 py-3 text-center">
                <ApprovalBadge status={entry.status} />
              </td>
              {renderActions(entry)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
