'use client'

import type { UseFormRegister, FieldErrors } from 'react-hook-form'
import type { CreateContractFormData } from '@/lib/validations/contract'

interface RetainerConfigFieldsProps {
  register: UseFormRegister<CreateContractFormData>
  errors: FieldErrors<CreateContractFormData>
  isSubmitting: boolean
  watchOverageAllowed: boolean
}

/**
 * Campos de configuração específicos para contrato Retainer (Banco de Horas).
 * Componente extraído do wizard para manter SRP.
 */
export default function RetainerConfigFields({
  register,
  errors,
  isSubmitting,
  watchOverageAllowed,
}: RetainerConfigFieldsProps) {
  const configErrors = errors.retainerConfig

  return (
    <fieldset className="space-y-4 rounded-lg border border-gray-200 p-4">
      <legend className="px-2 text-sm font-medium text-gray-700">
        Configuração do Banco de Horas
      </legend>

      <div className="grid gap-4 sm:grid-cols-2">
        {/* Horas mensais */}
        <div>
          <label
            htmlFor="monthlyHours"
            className="block text-sm font-medium text-gray-700"
          >
            Horas mensais
          </label>
          <input
            id="monthlyHours"
            type="number"
            min={1}
            placeholder="40"
            {...register('retainerConfig.monthlyHours', { valueAsNumber: true })}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {configErrors?.monthlyHours && (
            <p className="mt-1 text-sm text-red-600">
              {configErrors.monthlyHours.message}
            </p>
          )}
        </div>

        {/* Valor por hora */}
        <div>
          <label
            htmlFor="hourlyRate"
            className="block text-sm font-medium text-gray-700"
          >
            Valor por hora
          </label>
          <input
            id="hourlyRate"
            type="number"
            min={0}
            step="0.01"
            placeholder="150.00"
            {...register('retainerConfig.hourlyRate', { valueAsNumber: true })}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {configErrors?.hourlyRate && (
            <p className="mt-1 text-sm text-red-600">
              {configErrors.hourlyRate.message}
            </p>
          )}
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        {/* Política de rollover */}
        <div>
          <label
            htmlFor="rolloverPolicy"
            className="block text-sm font-medium text-gray-700"
          >
            Política de rollover
          </label>
          <select
            id="rolloverPolicy"
            {...register('retainerConfig.rolloverPolicy')}
            className="mt-1 block w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          >
            <option value="EXPIRE">Expirar</option>
            <option value="ACCUMULATE">Acumular</option>
            <option value="PARTIAL">Parcial</option>
          </select>
          {configErrors?.rolloverPolicy && (
            <p className="mt-1 text-sm text-red-600">
              {configErrors.rolloverPolicy.message}
            </p>
          )}
        </div>

        {/* Limite de alerta */}
        <div>
          <label
            htmlFor="alertThreshold"
            className="block text-sm font-medium text-gray-700"
          >
            Alerta de consumo (%)
          </label>
          <input
            id="alertThreshold"
            type="number"
            min={1}
            max={100}
            placeholder="80"
            {...register('retainerConfig.alertThreshold', { valueAsNumber: true })}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {configErrors?.alertThreshold && (
            <p className="mt-1 text-sm text-red-600">
              {configErrors.alertThreshold.message}
            </p>
          )}
        </div>
      </div>

      {/* Permite excedente */}
      <div className="flex items-center gap-3">
        <input
          id="overageAllowed"
          type="checkbox"
          {...register('retainerConfig.overageAllowed')}
          className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          disabled={isSubmitting}
        />
        <label
          htmlFor="overageAllowed"
          className="text-sm font-medium text-gray-700"
        >
          Permitir horas excedentes
        </label>
      </div>

      {/* Valor do excedente (visível apenas se overageAllowed) */}
      {watchOverageAllowed && (
        <div className="max-w-xs">
          <label
            htmlFor="overageRate"
            className="block text-sm font-medium text-gray-700"
          >
            Valor por hora excedente
          </label>
          <input
            id="overageRate"
            type="number"
            min={0}
            step="0.01"
            placeholder="200.00"
            {...register('retainerConfig.overageRate', { valueAsNumber: true })}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm placeholder:text-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
            disabled={isSubmitting}
          />
          {configErrors?.overageRate && (
            <p className="mt-1 text-sm text-red-600">
              {configErrors.overageRate.message}
            </p>
          )}
        </div>
      )}
    </fieldset>
  )
}
