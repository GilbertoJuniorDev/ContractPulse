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
    <fieldset className="space-y-4 rounded-xl border border-gray-200 p-4 dark:border-dark-border">
      <legend className="px-2 text-sm font-medium text-gray-700 dark:text-gray-300">
        Configuração do Banco de Horas
      </legend>

      <div className="grid gap-4 sm:grid-cols-2">
        {/* Horas mensais */}
        <div>
          <label
            htmlFor="monthlyHours"
            className="form-label"
          >
            Horas mensais
          </label>
          <input
            id="monthlyHours"
            type="number"
            min={1}
            placeholder="40"
            {...register('retainerConfig.monthlyHours', { valueAsNumber: true })}
            className="input-base"
            disabled={isSubmitting}
          />
          {configErrors?.monthlyHours && (
            <p className="field-error">
              {configErrors.monthlyHours.message}
            </p>
          )}
        </div>

        {/* Valor por hora */}
        <div>
          <label
            htmlFor="hourlyRate"
            className="form-label"
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
            className="input-base"
            disabled={isSubmitting}
          />
          {configErrors?.hourlyRate && (
            <p className="field-error">
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
            className="form-label"
          >
            Política de rollover
          </label>
          <select
            id="rolloverPolicy"
            {...register('retainerConfig.rolloverPolicy')}
            className="select-base"
            disabled={isSubmitting}
          >
            <option value="EXPIRE">Expirar</option>
            <option value="ACCUMULATE">Acumular</option>
            <option value="PARTIAL">Parcial</option>
          </select>
          {configErrors?.rolloverPolicy && (
            <p className="field-error">
              {configErrors.rolloverPolicy.message}
            </p>
          )}
        </div>

        {/* Limite de alerta */}
        <div>
          <label
            htmlFor="alertThreshold"
            className="form-label"
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
            className="input-base"
            disabled={isSubmitting}
          />
          {configErrors?.alertThreshold && (
            <p className="field-error">
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
          className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500 dark:border-dark-border dark:bg-dark-card"
          disabled={isSubmitting}
        />
        <label
          htmlFor="overageAllowed"
          className="text-sm font-medium text-gray-700 dark:text-gray-300"
        >
          Permitir horas excedentes
        </label>
      </div>

      {/* Valor do excedente (visível apenas se overageAllowed) */}
      {watchOverageAllowed && (
        <div className="max-w-xs">
          <label
            htmlFor="overageRate"
            className="form-label"
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
            className="input-base"
            disabled={isSubmitting}
          />
          {configErrors?.overageRate && (
            <p className="field-error">
              {configErrors.overageRate.message}
            </p>
          )}
        </div>
      )}
    </fieldset>
  )
}
