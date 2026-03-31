'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useCreateContract } from '@/hooks/useContracts'
import { useOrganizations } from '@/hooks/useOrganizations'
import { useClients } from '@/hooks/useClients'
import {
  createContractSchema,
  type CreateContractFormData,
} from '@/lib/validations/contract'
import RetainerConfigFields from './RetainerConfigFields'

interface ContractWizardProps {
  onSuccess?: () => void
  onCancel?: () => void
}

/**
 * Wizard de criação de contrato.
 * Usa React Hook Form + Zod para validação (padrão do projeto).
 * Atualmente suporta apenas tipo RETAINER (MVP).
 */
export default function ContractWizard({
  onSuccess,
  onCancel,
}: ContractWizardProps) {
  const [apiError, setApiError] = useState<string | null>(null)
  const createMutation = useCreateContract()
  const { data: organizations } = useOrganizations()
  const { data: clients, isLoading: isLoadingClients } = useClients()

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<CreateContractFormData>({
    resolver: zodResolver(createContractSchema),
    defaultValues: {
      type: 'RETAINER',
      currency: 'BRL',
      billingDay: 15,
      endDate: null,
      retainerConfig: {
        monthlyHours: 40,
        hourlyRate: 150,
        rolloverPolicy: 'EXPIRE',
        alertThreshold: 80,
        overageAllowed: false,
        overageRate: null,
      },
    },
  })

  const watchOverageAllowed = watch('retainerConfig.overageAllowed') ?? false

  async function onSubmit(data: CreateContractFormData) {
    setApiError(null)
    try {
      await createMutation.mutateAsync({
        organizationId: data.organizationId,
        clientUserId: data.clientUserId,
        title: data.title,
        type: data.type,
        currency: data.currency,
        billingDay: data.billingDay,
        startDate: data.startDate,
        endDate: data.endDate,
        retainerConfig: data.retainerConfig,
      })
      reset()
      onSuccess?.()
    } catch (error) {
      setApiError(
        error instanceof Error ? error.message : 'Erro ao criar contrato'
      )
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Dados básicos */}
      <div className="space-y-4">
        <h3 className="text-base font-medium text-gray-900">Dados do Contrato</h3>

        <div className="grid gap-4 sm:grid-cols-2">
          {/* Organização */}
          <div>
            <label
              htmlFor="organizationId"
              className="block text-sm font-medium text-gray-700"
            >
              Organização
            </label>
            <select
              id="organizationId"
              {...register('organizationId')}
              className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              disabled={isSubmitting}
            >
              <option value="">Selecione...</option>
              {organizations?.map((org) => (
                <option key={org.id} value={org.id}>
                  {org.name}
                </option>
              ))}
            </select>
            {errors.organizationId && (
              <p className="mt-1 text-sm text-red-600">
                {errors.organizationId.message}
              </p>
            )}
          </div>

          {/* Cliente */}
          <div>
            <label
              htmlFor="clientUserId"
              className="block text-sm font-medium text-gray-700"
            >
              Cliente
            </label>
            <select
              id="clientUserId"
              {...register('clientUserId')}
              className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              disabled={isSubmitting || isLoadingClients}
            >
              <option value="">
                {isLoadingClients ? 'Carregando clientes...' : 'Selecione o cliente...'}
              </option>
              {clients?.map((client) => (
                <option key={client.id} value={client.id}>
                  {client.fullName} ({client.email})
                </option>
              ))}
            </select>
            {errors.clientUserId && (
              <p className="mt-1 text-sm text-red-600">
                {errors.clientUserId.message}
              </p>
            )}
          </div>
        </div>

        {/* Título */}
        <div>
          <label
            htmlFor="title"
            className="block text-sm font-medium text-gray-700"
          >
            Título do Contrato
          </label>
          <input
            id="title"
            type="text"
            placeholder="Ex: Suporte Mensal - Empresa X"
            {...register('title')}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {errors.title && (
            <p className="mt-1 text-sm text-red-600">{errors.title.message}</p>
          )}
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          {/* Tipo (fixo em RETAINER para MVP) */}
          <div>
            <label
              htmlFor="type"
              className="block text-sm font-medium text-gray-700"
            >
              Tipo
            </label>
            <select
              id="type"
              {...register('type')}
              className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              disabled={isSubmitting}
            >
              <option value="RETAINER">Banco de Horas</option>
            </select>
          </div>

          {/* Moeda */}
          <div>
            <label
              htmlFor="currency"
              className="block text-sm font-medium text-gray-700"
            >
              Moeda
            </label>
            <select
              id="currency"
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
              htmlFor="billingDay"
              className="block text-sm font-medium text-gray-700"
            >
              Dia de faturamento
            </label>
            <input
              id="billingDay"
              type="number"
              min={1}
              max={28}
              {...register('billingDay', { valueAsNumber: true })}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              disabled={isSubmitting}
            />
            {errors.billingDay && (
              <p className="mt-1 text-sm text-red-600">
                {errors.billingDay.message}
              </p>
            )}
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          {/* Data de início */}
          <div>
            <label
              htmlFor="startDate"
              className="block text-sm font-medium text-gray-700"
            >
              Data de início
            </label>
            <input
              id="startDate"
              type="date"
              {...register('startDate')}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              disabled={isSubmitting}
            />
            {errors.startDate && (
              <p className="mt-1 text-sm text-red-600">
                {errors.startDate.message}
              </p>
            )}
          </div>

          {/* Data de fim (opcional) */}
          <div>
            <label
              htmlFor="endDate"
              className="block text-sm font-medium text-gray-700"
            >
              Data de fim{' '}
              <span className="text-gray-400">(opcional)</span>
            </label>
            <input
              id="endDate"
              type="date"
              {...register('endDate')}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              disabled={isSubmitting}
            />
          </div>
        </div>
      </div>

      {/* Configuração Retainer */}
      <RetainerConfigFields
        register={register}
        errors={errors}
        isSubmitting={isSubmitting}
        watchOverageAllowed={watchOverageAllowed}
      />

      {/* Erro de validação de refine (retainerConfig obrigatória) */}
      {errors.retainerConfig?.message && (
        <div className="rounded-md bg-red-50 p-3">
          <p className="text-sm text-red-700">{errors.retainerConfig.message}</p>
        </div>
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
          disabled={isSubmitting}
        >
          {isSubmitting ? 'Criando...' : 'Criar Contrato'}
        </button>
      </div>
    </form>
  )
}
