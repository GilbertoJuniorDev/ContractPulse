'use client'

import type { Contract } from '@/lib/types/contract'

interface TerminateContractDialogProps {
  contract: Contract
  isTerminating: boolean
  onConfirm: () => void
  onCancel: () => void
}

/**
 * Dialog de confirmação para encerrar um contrato.
 */
export default function TerminateContractDialog({
  contract,
  isTerminating,
  onConfirm,
  onCancel,
}: TerminateContractDialogProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="fixed inset-0 bg-black/50 transition-opacity"
        onClick={onCancel}
        aria-hidden="true"
      />

      <div className="relative z-50 mx-4 w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <div className="flex items-start gap-4">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-red-100">
            <svg
              className="h-5 w-5 text-red-600"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z"
              />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-semibold text-gray-900">
              Encerrar contrato
            </h3>
            <p className="mt-2 text-sm text-gray-500">
              Tem certeza que deseja encerrar o contrato{' '}
              <span className="font-medium text-gray-700">
                {contract.title}
              </span>
              ? Esta ação não pode ser desfeita. O contrato será finalizado permanentemente.
            </p>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isTerminating}
            className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isTerminating}
            className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 disabled:opacity-50"
          >
            {isTerminating ? 'Encerrando...' : 'Encerrar'}
          </button>
        </div>
      </div>
    </div>
  )
}
