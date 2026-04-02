'use client'

import type { Contract, ContractType } from '@/lib/types/contract'
import { formatCurrency, getCurrencyLabel } from '@/lib/utils/currency'
import { formatDate } from '@/lib/utils/date'
import ContractStatusBadge from './ContractStatusBadge'

interface ContractCardProps {
  contract: Contract
  onClick: (contract: Contract) => void
}

const TYPE_LABELS: Record<ContractType, string> = {
  RETAINER: 'Banco de Horas',
  FIXED_OVERAGE: 'Fixo + Excedente',
  PROFIT_SHARING: 'Profit Sharing',
}

const TYPE_COLORS: Record<ContractType, string> = {
  RETAINER: 'bg-blue-100 text-blue-700 dark:bg-blue-500/20 dark:text-blue-400',
  FIXED_OVERAGE: 'bg-indigo-100 text-indigo-700 dark:bg-indigo-500/20 dark:text-indigo-400',
  PROFIT_SHARING: 'bg-purple-100 text-purple-700 dark:bg-purple-500/20 dark:text-purple-400',
}

/**
 * Card de exibição resumida de um contrato na listagem.
 */
export default function ContractCard({ contract, onClick }: ContractCardProps) {
  const retainerConfig = contract.type === 'RETAINER' ? contract.config : null
  const monthlyHours =
    retainerConfig && typeof retainerConfig.monthlyHours === 'number'
      ? retainerConfig.monthlyHours
      : null
  const hourlyRate =
    retainerConfig && typeof retainerConfig.hourlyRate === 'number'
      ? retainerConfig.hourlyRate
      : null

  return (
    <button
      type="button"
      onClick={() => onClick(contract)}
      className="w-full rounded-xl border border-gray-200 bg-white p-5 text-left shadow-sm transition-all hover:shadow-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 dark:border-dark-border dark:bg-dark-card dark:shadow-none dark:hover:shadow-glow dark:focus:ring-offset-dark-bg"
    >
      <div className="flex items-start justify-between">
        <div className="min-w-0 flex-1">
          <h3 className="truncate text-lg font-semibold text-gray-900 dark:text-white">
            {contract.title}
          </h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Início: {formatDate(contract.startDate)}
            {contract.endDate && ` · Fim: ${formatDate(contract.endDate)}`}
          </p>
        </div>
        <ContractStatusBadge status={contract.status} />
      </div>

      <div className="mt-3 flex flex-wrap items-center gap-2">
        <span
          className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${TYPE_COLORS[contract.type]}`}
        >
          {TYPE_LABELS[contract.type]}
        </span>
        <span className="inline-flex items-center rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-700 dark:bg-gray-700 dark:text-gray-300">
          {getCurrencyLabel(contract.currency)}
        </span>
        <span className="text-xs text-gray-500 dark:text-gray-400">
          Faturamento dia {contract.billingDay}
        </span>
      </div>

      {contract.type === 'RETAINER' && monthlyHours !== null && hourlyRate !== null && (
        <div className="mt-3 flex items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
          <span>{monthlyHours}h/mês</span>
          <span>{formatCurrency(hourlyRate, contract.currency)}/h</span>
        </div>
      )}
    </button>
  )
}
