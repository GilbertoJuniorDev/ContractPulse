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
      <div className="mx-4 w-full max-w-md rounded-xl bg-white p-6 shadow-xl dark:bg-dark-card dark:ring-1 dark:ring-dark-border">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
          Disputar Lançamento
        </h3>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Informe o motivo para disputar este lançamento de horas.
        </p>

        <form onSubmit={handleSubmit(onSubmit)} className="mt-4 space-y-4">
          {apiError && (
            <div className="alert-error">
              {apiError}
            </div>
          )}

          <div>
            <label
              htmlFor="disputeReason"
              className="form-label"
            >
              Motivo da disputa
            </label>
            <textarea
              id="disputeReason"
              rows={4}
              placeholder="Descreva o motivo..."
              {...register('disputeReason')}
              className="input-base"
              disabled={isSubmitting}
            />
            {errors.disputeReason && (
              <p className="field-error">
                {errors.disputeReason.message}
              </p>
            )}
          </div>

          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-danger"
            >
              {isSubmitting ? 'Enviando...' : 'Disputar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
