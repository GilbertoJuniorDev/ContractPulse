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
      <div className="card">
        <h3 className="text-base font-semibold text-gray-900 dark:text-white">
          Pipeline de Aprovações
        </h3>
        <p className="mt-4 text-sm text-gray-500 dark:text-gray-400">Carregando...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="alert-error p-6">
        <p>Erro ao carregar pipeline: {error.message}</p>
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
    <div className="card">
      <h3 className="text-base font-semibold text-gray-900 dark:text-white">
        Pipeline de Aprovações
      </h3>

      {/* Contadores */}
      <div className="mt-4 grid grid-cols-2 gap-4">
        <div className="rounded-lg bg-blue-50 p-3 text-center dark:bg-blue-500/10">
          <p className="text-2xl font-bold text-blue-700 dark:text-blue-400">{submittedCount}</p>
          <p className="text-xs text-blue-600 dark:text-blue-400/70">Enviados</p>
        </div>
        <div className="rounded-lg bg-amber-50 p-3 text-center dark:bg-amber-500/10">
          <p className="text-2xl font-bold text-amber-700 dark:text-amber-400">
            {pendingApprovalCount}
          </p>
          <p className="text-xs text-amber-600 dark:text-amber-400/70">Pendentes</p>
        </div>
      </div>

      {/* Lista resumida */}
      {pendingEntries.length === 0 ? (
        <p className="mt-4 text-sm text-gray-500 dark:text-gray-400">
          Nenhum lançamento aguardando aprovação.
        </p>
      ) : (
        <ul className="mt-4 divide-y divide-gray-100 dark:divide-dark-border">
          {pendingEntries.slice(0, MAX_VISIBLE_ITEMS).map((entry) => (
            <li key={entry.id}>
              <Link
                href={`/contracts/${entry.contractId}/time-entries`}
                className="flex items-center justify-between py-3 rounded-lg px-2 -mx-2 transition-colors hover:bg-gray-50 dark:hover:bg-dark-hover"
              >
                <div className="min-w-0 flex-1">
                  <p
                    className="truncate text-sm font-medium text-gray-900 dark:text-white"
                    title={entry.description}
                  >
                    {entry.description}
                  </p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">
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
              <p className="text-xs text-gray-500 dark:text-gray-400">
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
