'use client'

import type { Organization } from '@/lib/types/organization'

interface OrganizationCardProps {
  organization: Organization
  onEdit: (organization: Organization) => void
  onDelete: (organization: Organization) => void
}

const PLAN_LABELS: Record<Organization['plan'], string> = {
  FREE: 'Gratuito',
  PRO: 'Profissional',
  ENTERPRISE: 'Empresarial',
}

const PLAN_COLORS: Record<Organization['plan'], string> = {
  FREE: 'bg-gray-100 text-gray-700',
  PRO: 'bg-blue-100 text-blue-700',
  ENTERPRISE: 'bg-purple-100 text-purple-700',
}

/**
 * Card que exibe os dados de uma organização na listagem.
 */
export default function OrganizationCard({
  organization,
  onEdit,
  onDelete,
}: OrganizationCardProps) {
  const createdDate = new Date(organization.createdAt).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md">
      <div className="flex items-start justify-between">
        <div className="min-w-0 flex-1">
          <h3 className="truncate text-lg font-semibold text-gray-900">
            {organization.name}
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Criada em {createdDate}
          </p>
        </div>
        <span
          className={`ml-3 inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${PLAN_COLORS[organization.plan]}`}
        >
          {PLAN_LABELS[organization.plan]}
        </span>
      </div>

      <div className="mt-4 flex items-center justify-end gap-2">
        <button
          onClick={() => onEdit(organization)}
          className="rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          Editar
        </button>
        <button
          onClick={() => onDelete(organization)}
          className="rounded-md border border-red-300 bg-white px-3 py-1.5 text-sm font-medium text-red-700 shadow-sm hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
        >
          Excluir
        </button>
      </div>
    </div>
  )
}
