'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useCreateOrganization } from '@/hooks/useOrganizations'
import {
  createOrganizationSchema,
  type CreateOrganizationFormData,
} from '@/lib/validations/organization'

interface CreateOrganizationFormProps {
  onSuccess?: () => void
  onCancel?: () => void
}

/**
 * Formulário de criação de organização.
 * Usa React Hook Form + Zod para validação.
 */
export default function CreateOrganizationForm({
  onSuccess,
  onCancel,
}: CreateOrganizationFormProps) {
  const [apiError, setApiError] = useState<string | null>(null)
  const createMutation = useCreateOrganization()

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<CreateOrganizationFormData>({
    resolver: zodResolver(createOrganizationSchema),
  })

  async function onSubmit(data: CreateOrganizationFormData) {
    setApiError(null)
    try {
      await createMutation.mutateAsync(data)
      reset()
      onSuccess?.()
    } catch (error) {
      setApiError(
        error instanceof Error ? error.message : 'Erro ao criar organização'
      )
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label
          htmlFor="org-name"
          className="block text-sm font-medium text-gray-700"
        >
          Nome da Organização
        </label>
        <input
          id="org-name"
          type="text"
          placeholder="Ex: Minha Agência"
          {...register('name')}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
          disabled={isSubmitting}
        />
        {errors.name && (
          <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
        )}
      </div>

      {apiError && (
        <div className="rounded-md bg-red-50 p-3">
          <p className="text-sm text-red-700">{apiError}</p>
        </div>
      )}

      <div className="flex justify-end gap-3">
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            disabled={isSubmitting}
          >
            Cancelar
          </button>
        )}
        <button
          type="submit"
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Criando...' : 'Criar Organização'}
        </button>
      </div>
    </form>
  )
}
