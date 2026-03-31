/**
 * Lista de lançamentos de horas com status e ações.
 */
'use client'

import { useTimeEntriesByContract } from '@/hooks/useTimeEntries'
import ApprovalBadge from './ApprovalBadge'

interface TimeEntryListProps {
  contractId: string
  showActions?: boolean
  onApprove?: (id: string) => void
  onDispute?: (id: string) => void
}

/**
 * Lista de lançamentos de horas de um contrato.
 */
export default function TimeEntryList({
  contractId,
  showActions = false,
  onApprove,
  onDispute,
}: TimeEntryListProps) {
  const { data: entries, isLoading, error } = useTimeEntriesByContract(contractId)

  if (isLoading) {
    return <p className="text-sm text-gray-500">Carregando lançamentos...</p>
  }

  if (error) {
    return (
      <p className="text-sm text-red-600">
        Erro ao carregar lançamentos: {error.message}
      </p>
    )
  }

  if (!entries || entries.length === 0) {
    return (
      <p className="text-sm text-gray-500">Nenhum lançamento encontrado.</p>
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">
              Data
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">
              Descrição
            </th>
            <th className="px-4 py-3 text-right text-xs font-medium uppercase text-gray-500">
              Horas
            </th>
            <th className="px-4 py-3 text-center text-xs font-medium uppercase text-gray-500">
              Status
            </th>
            {showActions && (
              <th className="px-4 py-3 text-center text-xs font-medium uppercase text-gray-500">
                Ações
              </th>
            )}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white">
          {entries.map((entry) => (
            <tr key={entry.id} className="hover:bg-gray-50">
              <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-900">
                {new Date(entry.entryDate).toLocaleDateString('pt-BR')}
              </td>
              <td className="px-4 py-3 text-sm text-gray-700">
                {entry.description}
              </td>
              <td className="whitespace-nowrap px-4 py-3 text-right text-sm font-medium text-gray-900">
                {entry.hours}h
              </td>
              <td className="whitespace-nowrap px-4 py-3 text-center">
                <ApprovalBadge status={entry.status} />
              </td>
              {showActions && entry.status === 'PENDING' && (
                <td className="whitespace-nowrap px-4 py-3 text-center">
                  <div className="flex justify-center gap-2">
                    <button
                      onClick={() => onApprove?.(entry.id)}
                      className="rounded bg-green-50 px-3 py-1 text-xs font-medium text-green-700 hover:bg-green-100"
                    >
                      Aprovar
                    </button>
                    <button
                      onClick={() => onDispute?.(entry.id)}
                      className="rounded bg-red-50 px-3 py-1 text-xs font-medium text-red-700 hover:bg-red-100"
                    >
                      Disputar
                    </button>
                  </div>
                </td>
              )}
              {showActions && entry.status !== 'PENDING' && (
                <td className="whitespace-nowrap px-4 py-3 text-center text-xs text-gray-400">
                  —
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
