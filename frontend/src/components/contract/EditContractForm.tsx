'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useUpdateContract } from '@/hooks/useContracts'
import {
  updateContractSchema,
  type UpdateContractFormData,
} from '@/lib/validations/contract'
import type { Contract } from '@/lib/types/contract'

interface EditContractFormProps {
  contract: Contract
  onSuccess?: () => void
  onCancel?: () => void
}

/**
 * Formulário de edição de contrato.
 * Pré-preenche com os dados existentes. Atualização parcial.
 */
export default function EditContractForm({
  contract,
  onSuccess,
  onCancel,
}: EditContractFormProps) {
  const [apiError, setApiError] = useState<string | null>(null)
  const updateMutation = useUpdateContract()

  const config = contract.config ?? {}
  const isRetainer = contract.type === 'RETAINER'

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting, isDirty },
  } = useForm<UpdateContractFormData>({
    resolver: zodResolver(updateContractSchema),
    defaultValues: {
      title: contract.title,
      currency: contract.currency,
      billingDay: contract.billingDay,
      endDate: contract.endDate,
      retainerConfig: isRetainer
        ? {
            monthlyHours: (config.monthlyHours as number) ?? 40,
            hourlyRate: (config.hourlyRate as number) ?? 150,
            rolloverPolicy:
              (config.rolloverPolicy as 'EXPIRE' | 'ACCUMULATE' | 'PARTIAL') ?? 'EXPIRE',
            alertThreshold: (config.alertThreshold as number) ?? 80,
            overageAllowed: (config.overageAllowed as boolean) ?? false,
            overageRate: (config.overageRate as number) ?? null,
          }
        : null,
    },
  })

  const watchOverageAllowed = watch('retainerConfig.overageAllowed') ?? false

  async function onSubmit(data: UpdateContractFormData) {
    setApiError(null)
    try {
      await updateMutation.mutateAsync({ id: contract.id, data })
      onSuccess?.()
    } catch (error) {
      setApiError(
        error instanceof Error ? error.message : 'Erro ao atualizar contrato'
      )
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Título */}
      <div>
        <label
          htmlFor="edit-title"
          className="block text-sm font-medium text-gray-700"
        >
          Título do Contrato
        </label>
        <input
          id="edit-title"
          type="text"
          {...register('title')}
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
          disabled={isSubmitting}
        />
        {errors.title && (
          <p className="mt-1 text-sm text-red-600">{errors.title.message}</p>
        )}
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        {/* Moeda */}
        <div>
          <label
            htmlFor="edit-currency"
            className="block text-sm font-medium text-gray-700"
          >
            Moeda
          </label>
          <select
            id="edit-currency"
            {...register('currency')}
            className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          >
            <option value="BRL">Real (R$)</option>
            <option value="USD">Dólar (US$)</option>
            <option value="EUR">Euro (€)</option>
          </select>
        </div>

        {/* Dia de faturamento */}
        <div>
          <label
            htmlFor="edit-billingDay"
            className="block text-sm font-medium text-gray-700"
          >
            Dia de faturamento
          </label>
          <input
            id="edit-billingDay"
            type="number"
            min={1}
            max={28}
            {...register('billingDay', { valueAsNumber: true })}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {errors.billingDay && (
            <p className="mt-1 text-sm text-red-600">
              {errors.billingDay.message}
            </p>
          )}
        </div>

        {/* Data de fim */}
        <div>
          <label
            htmlFor="edit-endDate"
            className="block text-sm font-medium text-gray-700"
          >
            Data de fim <span className="text-gray-400">(opcional)</span>
          </label>
          <input
            id="edit-endDate"
            type="date"
            {...register('endDate')}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
        </div>
      </div>

      {/* Configuração Retainer (se aplicável) */}
      {isRetainer && (
        <fieldset className="space-y-4 rounded-lg border border-gray-200 p-4">
          <legend className="px-2 text-sm font-medium text-gray-700">
            Configuração do Banco de Horas
          </legend>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label
                htmlFor="edit-monthlyHours"
                className="block text-sm font-medium text-gray-700"
              >
                Horas mensais
              </label>
              <input
                id="edit-monthlyHours"
                type="number"
                min={1}
                {...register('retainerConfig.monthlyHours', { valueAsNumber: true })}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
                disabled={isSubmitting}
              />
            </div>

            <div>
              <label
                htmlFor="edit-hourlyRate"
                className="block text-sm font-medium text-gray-700"
              >
                Valor por hora
              </label>
              <input
                id="edit-hourlyRate"
                type="number"
                min={0}
                step="0.01"
                {...register('retainerConfig.hourlyRate', { valueAsNumber: true })}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
                disabled={isSubmitting}
              />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label
                htmlFor="edit-rolloverPolicy"
                className="block text-sm font-medium text-gray-700"
              >
                Política de rollover
              </label>
              <select
                id="edit-rolloverPolicy"
                {...register('retainerConfig.rolloverPolicy')}
                className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
                disabled={isSubmitting}
              >
                <option value="EXPIRE">Expirar</option>
                <option value="ACCUMULATE">Acumular</option>
                <option value="PARTIAL">Parcial</option>
              </select>
            </div>

            <div>
              <label
                htmlFor="edit-alertThreshold"
                className="block text-sm font-medium text-gray-700"
              >
                Alerta de consumo (%)
              </label>
              <input
                id="edit-alertThreshold"
                type="number"
                min={1}
                max={100}
                {...register('retainerConfig.alertThreshold', { valueAsNumber: true })}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
                disabled={isSubmitting}
              />
            </div>
          </div>

          <div className="flex items-center gap-3">
            <input
              id="edit-overageAllowed"
              type="checkbox"
              {...register('retainerConfig.overageAllowed')}
              className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              disabled={isSubmitting}
            />
            <label
              htmlFor="edit-overageAllowed"
              className="text-sm font-medium text-gray-700"
            >
              Permitir horas excedentes
            </label>
          </div>

          {watchOverageAllowed && (
            <div className="max-w-xs">
              <label
                htmlFor="edit-overageRate"
                className="block text-sm font-medium text-gray-700"
              >
                Valor por hora excedente
              </label>
              <input
                id="edit-overageRate"
                type="number"
                min={0}
                step="0.01"
                {...register('retainerConfig.overageRate', { valueAsNumber: true })}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
                disabled={isSubmitting}
              />
            </div>
          )}
        </fieldset>
      )}

      {/* Erro da API */}
      {apiError && (
        <div className="rounded-md bg-red-50 p-3">
          <p className="text-sm text-red-700">{apiError}</p>
        </div>
      )}

      {/* Ações */}
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
