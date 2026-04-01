/**
 * Badge visual indicando o status de aprovação de um lançamento.
 *
 * Ciclo: DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 */
import type { TimeEntryStatus } from '@/lib/types/time-entry'

interface ApprovalBadgeProps {
  status: TimeEntryStatus
}

const statusConfig: Record<
  TimeEntryStatus,
  { label: string; className: string }
> = {
  DRAFT: {
    label: 'Rascunho',
    className: 'bg-gray-100 text-gray-800',
  },
  SUBMITTED: {
    label: 'Enviado',
    className: 'bg-blue-100 text-blue-800',
  },
  PENDING_APPROVAL: {
    label: 'Pendente',
    className: 'bg-yellow-100 text-yellow-800',
  },
  APPROVED: {
    label: 'Aprovado',
    className: 'bg-green-100 text-green-800',
  },
  DISPUTED: {
    label: 'Disputado',
    className: 'bg-red-100 text-red-800',
  },
  INVOICED: {
    label: 'Faturado',
    className: 'bg-purple-100 text-purple-800',
  },
}

/**
 * Badge visual para status de aprovação de lançamento de horas.
 */
export default function ApprovalBadge({ status }: ApprovalBadgeProps) {
  const config = statusConfig[status]

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${config.className}`}
    >
      {config.label}
    </span>
  )
}
