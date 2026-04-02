'use client'

import type { Contract } from '@/lib/types/contract'

interface ResumeContractDialogProps {
  contract: Contract
  isResuming: boolean
  onConfirm: () => void
  onCancel: () => void
}

/**
 * Dialog de confirmação para reativar um contrato pausado.
 */
export default function ResumeContractDialog({
  contract,
  isResuming,
  onConfirm,
  onCancel,
}: ResumeContractDialogProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="fixed inset-0 bg-black/50 transition-opacity"
        onClick={onCancel}
        aria-hidden="true"
      />

      <div className="relative z-50 mx-4 w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-dark-card dark:ring-1 dark:ring-dark-border">
        <div className="flex items-start gap-4">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-green-100 dark:bg-green-500/20">
            <svg
              className="h-5 w-5 text-green-600 dark:text-green-400"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M5.25 5.653c0-.856.917-1.398 1.667-.986l11.54 6.347a1.125 1.125 0 0 1 0 1.972l-11.54 6.347a1.125 1.125 0 0 1-1.667-.986V5.653Z"
              />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-semibold text-gray-900 dark:text-white">
              Reativar contrato
            </h3>
            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
              Tem certeza que deseja reativar o contrato{' '}
              <span className="font-medium text-gray-700 dark:text-gray-300">
                {contract.title}
              </span>
              ? Novos lançamentos de horas serão permitidos novamente.
            </p>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isResuming}
            className="btn-secondary"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isResuming}
            className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:opacity-50 dark:focus:ring-offset-dark-bg"
          >
            {isResuming ? 'Reativando...' : 'Reativar'}
          </button>
        </div>
      </div>
    </div>
  )
}
