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
          className="form-label"
        >
          Nome da Organização
        </label>
        <input
          id="edit-org-name"
          type="text"
          {...register('name')}
          className="input-base"
          disabled={isSubmitting}
        />
        {errors.name && (
          <p className="field-error">{errors.name.message}</p>
        )}
      </div>

      {apiError && (
        <div className="alert-error">
          <p>{apiError}</p>
        </div>
      )}

      <div className="flex justify-end gap-3">
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="btn-secondary"
            disabled={isSubmitting}
          >
            Cancelar
          </button>
        )}
        <button
          type="submit"
          className="btn-primary"
          disabled={isSubmitting || !isDirty}
        >
          {isSubmitting ? 'Salvando...' : 'Salvar Alterações'}
        </button>
      </div>
    </form>
  )
}
