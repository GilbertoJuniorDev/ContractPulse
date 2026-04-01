/**
 * Pipeline de aprovações pendentes para o dashboard do Provider.
 * Mostra lançamentos enviados aguardando revisão do cliente.
 */
'use client'

import Link from 'next/link'
import { useMyTimeEntries } from '@/hooks/useTimeEntries'
import ApprovalBadge from './ApprovalBadge'
import type { TimeEntryStatus } from '@/lib/types/time-entry'

/** Status que representam lançamentos "em trânsito" para o provider. */
const PENDING_STATUSES: TimeEntryStatus[] = ['SUBMITTED', 'PENDING_APPROVAL']

/** Máximo de itens exibidos na lista resumida. */
const MAX_VISIBLE_ITEMS = 10

/**
 * Pipeline visual de lançamentos aguardando aprovação.
 * Exibe contadores por status e lista resumida dos itens mais recentes.
 */
export default function PendingApprovalsPipeline() {
  const { data: entries, isLoading, error } = useMyTimeEntries()

  if (isLoading) {
    return (
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <h3 className="text-base font-semibold text-gray-900">
          Pipeline de Aprovações
        </h3>
        <p className="mt-4 text-sm text-gray-500">Carregando...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-6">
        <p className="text-sm text-red-600">
          Erro ao carregar pipeline: {error.message}
        </p>
      </div>
    )
  }

  const pendingEntries =
    entries?.filter((e) =>
      PENDING_STATUSES.includes(e.status as TimeEntryStatus)
    ) ?? []

  const submittedCount = pendingEntries.filter(
    (e) => e.status === 'SUBMITTED'
  ).length
  const pendingApprovalCount = pendingEntries.filter(
    (e) => e.status === 'PENDING_APPROVAL'
  ).length

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-6">
      <h3 className="text-base font-semibold text-gray-900">
        Pipeline de Aprovações
      </h3>

      {/* Contadores */}
      <div className="mt-4 grid grid-cols-2 gap-4">
        <div className="rounded-md bg-blue-50 p-3 text-center">
          <p className="text-2xl font-bold text-blue-700">{submittedCount}</p>
          <p className="text-xs text-blue-600">Enviados</p>
        </div>
        <div className="rounded-md bg-yellow-50 p-3 text-center">
          <p className="text-2xl font-bold text-yellow-700">
            {pendingApprovalCount}
          </p>
          <p className="text-xs text-yellow-600">Pendentes</p>
        </div>
      </div>

      {/* Lista resumida */}
      {pendingEntries.length === 0 ? (
        <p className="mt-4 text-sm text-gray-500">
          Nenhum lançamento aguardando aprovação.
        </p>
      ) : (
        <ul className="mt-4 divide-y divide-gray-100">
          {pendingEntries.slice(0, MAX_VISIBLE_ITEMS).map((entry) => (
            <li key={entry.id}>
              <Link
                href={`/contracts/${entry.contractId}/time-entries`}
                className="flex items-center justify-between py-3 rounded-md px-2 -mx-2 transition-colors hover:bg-gray-50"
              >
                <div className="min-w-0 flex-1">
                  <p
                    className="truncate text-sm font-medium text-gray-900"
                    title={entry.description}
                  >
                    {entry.description}
                  </p>
                  <p className="text-xs text-gray-500">
                    {formatShortDate(entry.entryDate)} •{' '}
                    {Number(entry.hours).toFixed(1)}h
                  </p>
                </div>
                <div className="ml-3 shrink-0">
                  <ApprovalBadge status={entry.status as TimeEntryStatus} />
                </div>
              </Link>
            </li>
          ))}
          {pendingEntries.length > MAX_VISIBLE_ITEMS && (
            <li className="py-3 text-center">
              <p className="text-xs text-gray-500">
                +{pendingEntries.length - MAX_VISIBLE_ITEMS} lançamentos
                adicionais
              </p>
            </li>
          )}
        </ul>
      )}
    </div>
  )
}

/**
 * Formata data ISO para dd/mm curto.
 */
function formatShortDate(dateStr: string): string {
  const date = new Date(dateStr + 'T00:00:00')
  return date.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
  })
}
