'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useUpdateOrganization } from '@/hooks/useOrganizations'
import {
  updateOrganizationSchema,
  type UpdateOrganizationFormData,
} from '@/lib/validations/organization'
import type { Organization } from '@/lib/types/organization'

interface EditOrganizationFormProps {
  organization: Organization
  onSuccess?: () => void
  onCancel?: () => void
}

/**
 * Formulário de edição de organização.
 * Pré-preenche com os dados existentes da organização.
 */
export default function EditOrganizationForm({
  organization,
  onSuccess,
  onCancel,
}: EditOrganizationFormProps) {
  const [apiError, setApiError] = useState<string | null>(null)
  const updateMutation = useUpdateOrganization()

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<UpdateOrganizationFormData>({
    resolver: zodResolver(updateOrganizationSchema),
    defaultValues: {
      name: organization.name,
    },
  })

  async function onSubmit(data: UpdateOrganizationFormData) {
    setApiError(null)
    try {
      await updateMutation.mutateAsync({ id: organization.id, data })
      onSuccess?.()
    } catch (error) {
      setApiError(
        error instanceof Error ? error.message : 'Erro ao atualizar organização'
      )
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label
          htmlFor="edit-org-name"
          className="block text-sm font-medium text-gray-700"
        >
          Nome da Organização
        </label>
        <input
          id="edit-org-name"
          type="text"
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
          disabled={isSubmitting || !isDirty}
        >
          {isSubmitting ? 'Salvando...' : 'Salvar Alterações'}
        </button>
      </div>
    </form>
  )
}
