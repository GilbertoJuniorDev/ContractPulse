'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useDisputeTimeEntry } from '@/hooks/useTimeEntries'
import {
  disputeTimeEntrySchema,
  type DisputeTimeEntryFormData,
} from '@/lib/validations/time-entry'

interface DisputeModalProps {
  timeEntryId: string
  onClose: () => void
  onSuccess?: () => void
}

/**
 * Modal para disputar um lançamento de horas com justificativa.
 */
export default function DisputeModal({
  timeEntryId,
  onClose,
  onSuccess,
}: DisputeModalProps) {
  const [apiError, setApiError] = useState<string | null>(null)
  const disputeMutation = useDisputeTimeEntry()

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DisputeTimeEntryFormData>({
    resolver: zodResolver(disputeTimeEntrySchema),
  })

  async function onSubmit(data: DisputeTimeEntryFormData) {
    setApiError(null)
    try {
      await disputeMutation.mutateAsync({
        id: timeEntryId,
        data: { disputeReason: data.disputeReason },
      })
      onSuccess?.()
      onClose()
    } catch (error) {
      setApiError(
        error instanceof Error ? error.message : 'Erro ao disputar lançamento'
      )
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="mx-4 w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold text-gray-900">
          Disputar Lançamento
        </h3>
        <p className="mt-1 text-sm text-gray-500">
          Informe o motivo para disputar este lançamento de horas.
        </p>

        <form onSubmit={handleSubmit(onSubmit)} className="mt-4 space-y-4">
          {apiError && (
            <div className="rounded-md bg-red-50 p-3 text-sm text-red-700">
              {apiError}
            </div>
          )}

          <div>
            <label
              htmlFor="disputeReason"
              className="block text-sm font-medium text-gray-700"
            >
              Motivo da disputa
            </label>
            <textarea
              id="disputeReason"
              rows={4}
              placeholder="Descreva o motivo..."
              {...register('disputeReason')}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              disabled={isSubmitting}
            />
            {errors.disputeReason && (
              <p className="mt-1 text-sm text-red-600">
                {errors.disputeReason.message}
              </p>
            )}
          </div>

          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
            >
              {isSubmitting ? 'Enviando...' : 'Disputar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
