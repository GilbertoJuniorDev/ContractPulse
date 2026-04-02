'use client'

import { useState } from 'react'
import { useOrganizations } from '@/hooks/useOrganizations'
import {
  useContractsByOrganization,
} from '@/hooks/useContracts'
import type { Contract } from '@/lib/types/contract'
import ContractCard from './ContractCard'

interface ContractListProps {
  onCreateClick: () => void
  onContractClick: (contractId: string) => void
}

/**
 * Container que gerencia a listagem de contratos filtrada por organização.
 * Sem lógica de negócio — apenas orquestra UI e hooks.
 */
export default function ContractList({
  onCreateClick,
  onContractClick,
}: ContractListProps) {
  const { data: organizations, isLoading: isLoadingOrgs } = useOrganizations()
  const [selectedOrgId, setSelectedOrgId] = useState<string>('')

  // Seleciona a primeira organização automaticamente ao carregar
  const activeOrgId =
    selectedOrgId || (organizations && organizations.length > 0 ? organizations[0].id : '')

  const {
    data: contracts,
    isLoading: isLoadingContracts,
    error,
  } = useContractsByOrganization(activeOrgId)

  function handleContractClick(contract: Contract) {
    onContractClick(contract.id)
  }

  if (isLoadingOrgs) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <span className="ml-3 text-sm text-gray-600 dark:text-gray-400">
          Carregando organizações...
        </span>
      </div>
    )
  }

  if (!organizations || organizations.length === 0) {
    return (
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
          Nenhuma organização encontrada
        </h3>
        <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
          Crie uma organização em Configurações antes de criar contratos.
        </p>
      </div>
    )
  }

  return (
    <div>
      {/* Header com filtro de organização e botão de criação */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            Seus Contratos
          </h2>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Gerencie os contratos da sua organização.
          </p>
        </div>
        <div className="flex items-center gap-3">
          {organizations.length > 1 && (
            <select
              value={activeOrgId}
              onChange={(e) => setSelectedOrgId(e.target.value)}
              className="select-base"
            >
              {organizations.map((org) => (
                <option key={org.id} value={org.id}>
                  {org.name}
                </option>
              ))}
            </select>
          )}
          <button
            onClick={onCreateClick}
            className="btn-primary"
          >
            Novo Contrato
          </button>
        </div>
      </div>

      {/* Loading */}
      {isLoadingContracts && (
        <div className="flex items-center justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
          <span className="ml-3 text-sm text-gray-600 dark:text-gray-400">
            Carregando contratos...
          </span>
        </div>
      )}

      {/* Erro */}
      {error && (
        <div className="alert-error">
          <p>
            Erro ao carregar contratos:{' '}
            {error instanceof Error ? error.message : 'Erro desconhecido'}
          </p>
        </div>
      )}

      {/* Lista vazia */}
      {!isLoadingContracts && !error && contracts && contracts.length === 0 && (
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
              d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"
            />
          </svg>
          <h3 className="mt-4 text-sm font-medium text-gray-900 dark:text-white">
            Nenhum contrato
          </h3>
          <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
            Crie seu primeiro contrato para começar a gerenciar horas e faturamento.
          </p>
          <button
            onClick={onCreateClick}
            className="btn-primary"
          >
            Criar Contrato
          </button>
        </div>
      )}

      {/* Grid de cards */}
      {!isLoadingContracts && !error && contracts && contracts.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {contracts.map((contract) => (
            <ContractCard
              key={contract.id}
              contract={contract}
              onClick={handleContractClick}
            />
          ))}
        </div>
      )}
    </div>
  )
}
