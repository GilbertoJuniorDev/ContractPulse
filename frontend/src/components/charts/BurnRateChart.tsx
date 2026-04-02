/**
 * Gráfico de burn rate (consumo de horas/orçamento).
 */
'use client'

import type { ClientDashboard } from '@/lib/types/time-entry'

interface BurnRateChartProps {
  dashboard: ClientDashboard
}

/**
 * Exibe um gráfico visual de burn rate (barra de progresso + métricas).
 * Versão MVP com barra de progresso — Recharts será adicionado na V1.
 */
export default function BurnRateChart({ dashboard }: BurnRateChartProps) {
  const burnRate = Math.min(dashboard.burnRatePercentage, 100)
  const isOverBudget = dashboard.burnRatePercentage > 100

  function getBurnRateColor(): string {
    if (isOverBudget) return 'bg-red-500'
    if (burnRate >= 80) return 'bg-orange-500'
    return 'bg-blue-500'
  }

  return (
    <div className="card">
      <h3 className="text-sm font-medium text-gray-500 dark:text-gray-400">Burn Rate</h3>

      <div className="mt-4">
        <div className="flex justify-between text-sm">
          <span className="font-medium text-gray-900 dark:text-white">
            {dashboard.approvedHoursThisPeriod}h usadas
          </span>
          <span className="text-gray-500 dark:text-gray-400">
            de {dashboard.monthlyHours}h contratadas
          </span>
        </div>

        <div className="mt-2 h-3 w-full overflow-hidden rounded-full bg-gray-200 dark:bg-dark-border">
          <div
            className={`h-full rounded-full transition-all duration-500 ${getBurnRateColor()}`}
            style={{ width: `${Math.min(burnRate, 100)}%` }}
          />
        </div>

        <div className="mt-2 flex justify-between text-xs text-gray-500 dark:text-gray-400">
          <span>{burnRate.toFixed(1)}% consumido</span>
          <span>{dashboard.remainingHours}h restantes</span>
        </div>
      </div>

      {isOverBudget && (
        <p className="mt-3 text-xs font-medium text-red-600 dark:text-red-400">
          Horas excedidas em{' '}
          {(dashboard.burnRatePercentage - 100).toFixed(1)}%
        </p>
      )}
    </div>
  )
}
