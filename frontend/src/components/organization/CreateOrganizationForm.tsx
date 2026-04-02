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
          className="form-label"
        >
          Nome da Organização
        </label>
        <input
          id="org-name"
          type="text"
          placeholder="Ex: Minha Agência"
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
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Criando...' : 'Criar Organização'}
        </button>
      </div>
    </form>
  )
}
