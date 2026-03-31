'use client'

import type { ContractStatus } from '@/lib/types/contract'

const STATUS_LABELS: Record<ContractStatus, string> = {
  ACTIVE: 'Ativo',
  PAUSED: 'Pausado',
  TERMINATED: 'Encerrado',
}

const STATUS_COLORS: Record<ContractStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  PAUSED: 'bg-yellow-100 text-yellow-700',
  TERMINATED: 'bg-red-100 text-red-700',
}

interface ContractStatusBadgeProps {
  status: ContractStatus
}

/**
 * Badge visual para exibição do status de um contrato.
 */
export default function ContractStatusBadge({
  status,
}: ContractStatusBadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${STATUS_COLORS[status]}`}
    >
      {STATUS_LABELS[status]}
    </span>
  )
}
