'use client'

import type { Contract } from '@/lib/types/contract'

interface PauseContractDialogProps {
  contract: Contract
  isPausing: boolean
  onConfirm: () => void
  onCancel: () => void
}

/**
 * Dialog de confirmação para pausar um contrato ativo.
 */
export default function PauseContractDialog({
  contract,
  isPausing,
  onConfirm,
  onCancel,
}: PauseContractDialogProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="fixed inset-0 bg-black/50 transition-opacity"
        onClick={onCancel}
        aria-hidden="true"
      />

      <div className="relative z-50 mx-4 w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-dark-card dark:ring-1 dark:ring-dark-border">
        <div className="flex items-start gap-4">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-yellow-100 dark:bg-amber-500/20">
            <svg
              className="h-5 w-5 text-yellow-600 dark:text-amber-400"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M15.75 5.25v13.5m-7.5-13.5v13.5"
              />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-semibold text-gray-900 dark:text-white">
              Pausar contrato
            </h3>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
              Tem certeza que deseja pausar o contrato{' '}
              <span className="font-medium text-gray-700 dark:text-gray-300">
                {contract.title}
              </span>
              ? Novos lançamentos de horas serão bloqueados enquanto pausado.
            </p>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isPausing}
            className="btn-secondary"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isPausing}
            className="rounded-md bg-yellow-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:ring-offset-2 disabled:opacity-50 dark:focus:ring-offset-dark-bg"
          >
            {isPausing ? 'Pausando...' : 'Pausar'}
          </button>
        </div>
      </div>
    </div>
  )
}
