'use client'

import { useState } from 'react'
import {
  useOrganizations,
  useDeleteOrganization,
} from '@/hooks/useOrganizations'
import type { Organization } from '@/lib/types/organization'
import OrganizationCard from './OrganizationCard'
import CreateOrganizationForm from './CreateOrganizationForm'
import EditOrganizationForm from './EditOrganizationForm'
import DeleteOrganizationDialog from './DeleteOrganizationDialog'

type ViewState =
  | { mode: 'list' }
  | { mode: 'create' }
  | { mode: 'edit'; organization: Organization }
  | { mode: 'delete'; organization: Organization }

/**
 * Container que gerencia a listagem, criação, edição e exclusão de organizações.
 * Sem lógica de negócio — apenas orquestra UI e hooks.
 */
export default function OrganizationList() {
  const [view, setView] = useState<ViewState>({ mode: 'list' })
  const { data: organizations, isLoading, error } = useOrganizations()
  const deleteMutation = useDeleteOrganization()

  function handleBackToList() {
    setView({ mode: 'list' })
  }

  async function handleConfirmDelete(organizationId: string) {
    await deleteMutation.mutateAsync(organizationId)
    setView({ mode: 'list' })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <span className="ml-3 text-sm text-gray-600 dark:text-gray-400">
          Carregando organizações...
        </span>
      </div>
    )
  }

  if (error) {
    return (
      <div className="alert-error">
        <p>
          Erro ao carregar organizações:{' '}
          {error instanceof Error ? error.message : 'Erro desconhecido'}
        </p>
      </div>
    )
  }

  // Modo criação
  if (view.mode === 'create') {
    return (
      <div>
        <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">
          Nova Organização
        </h2>
        <div className="card">
          <CreateOrganizationForm
            onSuccess={handleBackToList}
            onCancel={handleBackToList}
          />
        </div>
      </div>
    )
  }

  // Modo edição
  if (view.mode === 'edit') {
    return (
      <div>
        <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">
          Editar Organização
        </h2>
        <div className="card">
          <EditOrganizationForm
            organization={view.organization}
            onSuccess={handleBackToList}
            onCancel={handleBackToList}
          />
        </div>
      </div>
    )
  }

  // Modo exclusão (dialog de confirmação)
  if (view.mode === 'delete') {
    return (
      <>
        <OrganizationListContent
          organizations={organizations ?? []}
          onCreateClick={() => setView({ mode: 'create' })}
          onEdit={(org) => setView({ mode: 'edit', organization: org })}
          onDelete={(org) => setView({ mode: 'delete', organization: org })}
        />
        <DeleteOrganizationDialog
          organization={view.organization}
          isDeleting={deleteMutation.isPending}
          onConfirm={() => handleConfirmDelete(view.organization.id)}
          onCancel={handleBackToList}
        />
      </>
    )
  }

  // Modo listagem (padrão)
  return (
    <OrganizationListContent
      organizations={organizations ?? []}
      onCreateClick={() => setView({ mode: 'create' })}
      onEdit={(org) => setView({ mode: 'edit', organization: org })}
      onDelete={(org) => setView({ mode: 'delete', organization: org })}
    />
  )
}

interface OrganizationListContentProps {
  organizations: Organization[]
  onCreateClick: () => void
  onEdit: (organization: Organization) => void
  onDelete: (organization: Organization) => void
}

/**
 * Conteúdo da listagem de organizações — extrai a renderização do container.
 */
function OrganizationListContent({
  organizations,
  onCreateClick,
  onEdit,
  onDelete,
}: OrganizationListContentProps) {
  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            Suas Organizações
          </h2>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Gerencie as organizações vinculadas à sua conta.
          </p>
        </div>
        <button
          onClick={onCreateClick}
          className="btn-primary"
        >
          Nova Organização
        </button>
      </div>

      {organizations.length === 0 ? (
        <div className="rounded-xl border-2 border-dashed border-gray-300 p-12 text-center dark:border-dark-border">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0H5m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
            />
          </svg>
          <h3 className="mt-4 text-sm font-medium text-gray-900 dark:text-white">
            Nenhuma organização
          </h3>
          <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
            Crie sua primeira organização para começar a gerenciar contratos.
          </p>
          <button
            onClick={onCreateClick}
            className="btn-primary mt-4"
          >
            Criar Organização
          </button>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {organizations.map((org) => (
            <OrganizationCard
              key={org.id}
              organization={org}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </div>
  )
}
