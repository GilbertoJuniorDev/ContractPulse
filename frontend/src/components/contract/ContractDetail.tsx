'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import {
  useContract,
  usePauseContract,
  useResumeContract,
  useTerminateContract,
} from '@/hooks/useContracts'
import type { Contract, ContractType, RolloverPolicy } from '@/lib/types/contract'
import { formatCurrency } from '@/lib/utils/currency'
import { formatDate, formatDateTime } from '@/lib/utils/date'
import ContractStatusBadge from './ContractStatusBadge'
import EditContractForm from './EditContractForm'
import PauseContractDialog from './PauseContractDialog'
import ResumeContractDialog from './ResumeContractDialog'
import TerminateContractDialog from './TerminateContractDialog'

interface ContractDetailProps {
  contractId: string
  onBack: () => void
}

type ViewState =
  | { mode: 'view' }
  | { mode: 'edit' }
  | { mode: 'pause' }
  | { mode: 'resume' }
  | { mode: 'terminate' }

const TYPE_LABELS: Record<ContractType, string> = {
  RETAINER: 'Banco de Horas',
  FIXED_OVERAGE: 'Fixo + Excedente',
  PROFIT_SHARING: 'Profit Sharing',
}

const ROLLOVER_LABELS: Record<RolloverPolicy, string> = {
  EXPIRE: 'Expirar',
  ACCUMULATE: 'Acumular',
  PARTIAL: 'Parcial',
}

/**
 * Componente de detalhe de contrato com ações de edição, pausa e encerramento.
 */
export default function ContractDetail({
  contractId,
  onBack,
}: ContractDetailProps) {
  const router = useRouter()
  const [view, setView] = useState<ViewState>({ mode: 'view' })
  const [copiedLink, setCopiedLink] = useState(false)
  const { data: contract, isLoading, error } = useContract(contractId)
  const pauseMutation = usePauseContract()
  const resumeMutation = useResumeContract()
  const terminateMutation = useTerminateContract()

  function handleBackToView() {
    setView({ mode: 'view' })
  }

  async function handleConfirmPause() {
    await pauseMutation.mutateAsync(contractId)
    setView({ mode: 'view' })
  }

  async function handleConfirmResume() {
    await resumeMutation.mutateAsync(contractId)
    setView({ mode: 'view' })
  }

  async function handleConfirmTerminate() {
    await terminateMutation.mutateAsync(contractId)
    setView({ mode: 'view' })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
        <span className="ml-3 text-sm text-gray-600">
          Carregando contrato...
        </span>
      </div>
    )
  }

  if (error || !contract) {
    return (
      <div className="space-y-4">
        <div className="rounded-md bg-red-50 p-4">
          <p className="text-sm text-red-700">
            {error instanceof Error
              ? error.message
              : 'Contrato não encontrado.'}
          </p>
        </div>
        <button
          onClick={onBack}
          className="text-sm font-medium text-blue-600 hover:text-blue-800"
        >
          &larr; Voltar para contratos
        </button>
      </div>
    )
  }

  // Modo edição
  if (view.mode === 'edit') {
    return (
      <div>
        <button
          onClick={handleBackToView}
          className="mb-4 text-sm font-medium text-blue-600 hover:text-blue-800"
        >
          &larr; Voltar ao detalhe
        </button>
        <h2 className="mb-4 text-lg font-semibold text-gray-900">
          Editar Contrato
        </h2>
        <div className="rounded-lg border border-gray-200 bg-white p-6">
          <EditContractForm
            contract={contract}
            onSuccess={handleBackToView}
            onCancel={handleBackToView}
          />
        </div>
      </div>
    )
  }

  const isActive = contract.status === 'ACTIVE'
  const isPaused = contract.status === 'PAUSED'
  const isPausedOrActive =
    contract.status === 'ACTIVE' || contract.status === 'PAUSED'
  const config = contract.config ?? {}

  return (
    <>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <button
              onClick={onBack}
              className="mb-2 text-sm font-medium text-blue-600 hover:text-blue-800"
            >
              &larr; Voltar para contratos
            </button>
            <h2 className="text-2xl font-bold text-gray-900">
              {contract.title}
            </h2>
            <div className="mt-2 flex items-center gap-3">
              <ContractStatusBadge status={contract.status} />
              <span className="text-sm text-gray-500">
                {TYPE_LABELS[contract.type]}
              </span>
            </div>
          </div>

          {/* Ações */}
          <div className="flex items-center gap-2">
            {isPausedOrActive && (
              <button
                onClick={() => {
                  const url = `${window.location.origin}/client-view/${contractId}`
                  void navigator.clipboard.writeText(url)
                  setCopiedLink(true)
                  setTimeout(() => setCopiedLink(false), 2000)
                }}
                className="rounded-md border border-indigo-300 bg-white px-3 py-1.5 text-sm font-medium text-indigo-700 shadow-sm hover:bg-indigo-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
              >
                {copiedLink ? '✓ Link copiado' : 'Copiar Link do Cliente'}
              </button>
            )}
            {isPausedOrActive && (
              <button
                onClick={() => router.push(`/client-view/${contractId}`)}
                className="rounded-md border border-teal-300 bg-white px-3 py-1.5 text-sm font-medium text-teal-700 shadow-sm hover:bg-teal-50 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:ring-offset-2"
              >
                Visão do Cliente
              </button>
            )}
            {isActive && (
              <>
                <button
                  onClick={() => router.push(`/contracts/${contractId}/time-entries`)}
                  className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                  Lançar Horas
                </button>
                <button
                  onClick={() => setView({ mode: 'edit' })}
                  className="rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                  Editar
                </button>
                <button
                  onClick={() => setView({ mode: 'pause' })}
                  className="rounded-md border border-yellow-300 bg-white px-3 py-1.5 text-sm font-medium text-yellow-700 shadow-sm hover:bg-yellow-50 focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:ring-offset-2"
                >
                  Pausar
                </button>
              </>
            )}
            {isPaused && (
              <button
                onClick={() => setView({ mode: 'resume' })}
                className="rounded-md border border-green-300 bg-white px-3 py-1.5 text-sm font-medium text-green-700 shadow-sm hover:bg-green-50 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
              >
                Reativar
              </button>
            )}
            {isPausedOrActive && contract.status !== 'TERMINATED' && (
              <button
                onClick={() => setView({ mode: 'terminate' })}
                className="rounded-md border border-red-300 bg-white px-3 py-1.5 text-sm font-medium text-red-700 shadow-sm hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
              >
                Encerrar
              </button>
            )}
          </div>
        </div>

        {/* Informações gerais */}
        <div className="rounded-lg border border-gray-200 bg-white p-6">
          <h3 className="mb-4 text-base font-semibold text-gray-900">
            Informações Gerais
          </h3>
          <dl className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <InfoItem label="Moeda" value={contract.currency} />
            <InfoItem
              label="Dia de faturamento"
              value={`Dia ${contract.billingDay}`}
            />
            <InfoItem
              label="Data de início"
              value={formatDate(contract.startDate)}
            />
            <InfoItem
              label="Data de fim"
              value={contract.endDate ? formatDate(contract.endDate) : 'Indeterminado'}
            />
            <InfoItem
              label="Criado em"
              value={formatDateTime(contract.createdAt)}
            />
            <InfoItem
              label="Atualizado em"
              value={formatDateTime(contract.updatedAt)}
            />
          </dl>
        </div>

        {/* Configuração Retainer */}
        {contract.type === 'RETAINER' && (
          <div className="rounded-lg border border-gray-200 bg-white p-6">
            <h3 className="mb-4 text-base font-semibold text-gray-900">
              Configuração — Banco de Horas
            </h3>
            <dl className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <InfoItem
                label="Horas mensais"
                value={`${config.monthlyHours ?? '-'}h`}
              />
              <InfoItem
                label="Valor por hora"
                value={
                  typeof config.hourlyRate === 'number'
                    ? formatCurrency(config.hourlyRate, contract.currency)
                    : '-'
                }
              />
              <InfoItem
                label="Política de rollover"
                value={
                  typeof config.rolloverPolicy === 'string'
                    ? ROLLOVER_LABELS[config.rolloverPolicy as RolloverPolicy] ??
                      String(config.rolloverPolicy)
                    : '-'
                }
              />
              <InfoItem
                label="Alerta de consumo"
                value={`${config.alertThreshold ?? 80}%`}
              />
              <InfoItem
                label="Permite excedente"
                value={config.overageAllowed ? 'Sim' : 'Não'}
              />
              {Boolean(config.overageAllowed) && typeof config.overageRate === 'number' && (
                <InfoItem
                  label="Valor hora excedente"
                  value={formatCurrency(config.overageRate, contract.currency)}
                />
              )}
            </dl>
          </div>
        )}
      </div>

      {/* Dialogs */}
      {view.mode === 'pause' && (
        <PauseContractDialog
          contract={contract}
          isPausing={pauseMutation.isPending}
          onConfirm={handleConfirmPause}
          onCancel={handleBackToView}
        />
      )}
      {view.mode === 'resume' && (
        <ResumeContractDialog
          contract={contract}
          isResuming={resumeMutation.isPending}
          onConfirm={handleConfirmResume}
          onCancel={handleBackToView}
        />
      )}
      {view.mode === 'terminate' && (
        <TerminateContractDialog
          contract={contract}
          isTerminating={terminateMutation.isPending}
          onConfirm={handleConfirmTerminate}
          onCancel={handleBackToView}
        />
      )}
    </>
  )
}

interface InfoItemProps {
  label: string
  value: string | number
}

function InfoItem({ label, value }: InfoItemProps) {
  return (
    <div>
      <dt className="text-sm font-medium text-gray-500">{label}</dt>
      <dd className="mt-1 text-sm text-gray-900">{String(value)}</dd>
    </div>
  )
}
