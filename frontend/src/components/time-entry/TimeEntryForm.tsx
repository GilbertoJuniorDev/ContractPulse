/**
 * Formulário de lançamento de horas (React Hook Form + Zod).
 */
'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useCreateTimeEntry } from '@/hooks/useTimeEntries'
import {
  createTimeEntrySchema,
  type CreateTimeEntryFormData,
} from '@/lib/validations/time-entry'

interface TimeEntryFormProps {
  contractId: string
  onSuccess?: () => void
  onCancel?: () => void
}

/**
 * Formulário de lançamento de horas pelo provider.
 */
export default function TimeEntryForm({
  contractId,
  onSuccess,
  onCancel,
}: TimeEntryFormProps) {
  const [apiError, setApiError] = useState<string | null>(null)
  const createMutation = useCreateTimeEntry()

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<CreateTimeEntryFormData>({
    resolver: zodResolver(createTimeEntrySchema),
    defaultValues: {
      contractId,
      hours: 1,
      entryDate: new Date().toISOString().split('T')[0],
    },
  })

  async function onSubmit(data: CreateTimeEntryFormData) {
    setApiError(null)
    try {
      await createMutation.mutateAsync({
        contractId: data.contractId,
        description: data.description,
        hours: data.hours,
        entryDate: data.entryDate,
      })
      reset()
      onSuccess?.()
    } catch (error) {
      setApiError(
        error instanceof Error ? error.message : 'Erro ao lançar horas'
      )
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      {apiError && (
        <div className="alert-error">
          {apiError}
        </div>
      )}

      <input type="hidden" {...register('contractId')} />

      <div>
        <label
          htmlFor="description"
          className="form-label"
        >
          Descrição
        </label>
        <textarea
          id="description"
          rows={3}
          placeholder="Ex: Desenvolvimento da feature de relatórios"
          {...register('description')}
          className="input-base"
          disabled={isSubmitting}
        />
        {errors.description && (
          <p className="field-error">
            {errors.description.message}
          </p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label
            htmlFor="hours"
            className="form-label"
          >
            Horas
          </label>
          <input
            id="hours"
            type="number"
            step="0.5"
            min="0.5"
            max="24"
            {...register('hours', { valueAsNumber: true })}
            className="input-base"
            disabled={isSubmitting}
          />
          {errors.hours && (
            <p className="field-error">
              {errors.hours.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="entryDate"
            className="form-label"
          >
            Data
          </label>
          <input
            id="entryDate"
            type="date"
            {...register('entryDate')}
            className="input-base"
            disabled={isSubmitting}
          />
          {errors.entryDate && (
            <p className="field-error">
              {errors.entryDate.message}
            </p>
          )}
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-2">
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="btn-secondary"
          >
            Cancelar
          </button>
        )}
        <button
          type="submit"
          disabled={isSubmitting}
          className="btn-primary"
        >
          {isSubmitting ? 'Salvando...' : 'Lançar Horas'}
        </button>
      </div>
    </form>
  )
}
