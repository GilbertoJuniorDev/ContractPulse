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
        <div className="rounded-md bg-red-50 p-3 text-sm text-red-700">
          {apiError}
        </div>
      )}

      <input type="hidden" {...register('contractId')} />

      <div>
        <label
          htmlFor="description"
          className="block text-sm font-medium text-gray-700"
        >
          Descrição
        </label>
        <textarea
          id="description"
          rows={3}
          placeholder="Ex: Desenvolvimento da feature de relatórios"
          {...register('description')}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
          disabled={isSubmitting}
        />
        {errors.description && (
          <p className="mt-1 text-sm text-red-600">
            {errors.description.message}
          </p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label
            htmlFor="hours"
            className="block text-sm font-medium text-gray-700"
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
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {errors.hours && (
            <p className="mt-1 text-sm text-red-600">
              {errors.hours.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="entryDate"
            className="block text-sm font-medium text-gray-700"
          >
            Data
          </label>
          <input
            id="entryDate"
            type="date"
            {...register('entryDate')}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {errors.entryDate && (
            <p className="mt-1 text-sm text-red-600">
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
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Cancelar
          </button>
        )}
        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {isSubmitting ? 'Salvando...' : 'Lançar Horas'}
        </button>
      </div>
    </form>
  )
}
