/**
 * Visão geral do Provider — dashboard principal com métricas e pipeline.
 */
'use client'

import PendingApprovalsPipeline from '@/components/time-entry/PendingApprovalsPipeline'

export default function OverviewPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Visão Geral</h1>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Pipeline de aprovações pendentes */}
        <PendingApprovalsPipeline />

        {/* Placeholder para faturamento projetado — a ser implementado */}
        <div className="rounded-lg border-2 border-dashed border-gray-300 p-6">
          <h3 className="text-base font-semibold text-gray-400">
            Faturamento Projetado
          </h3>
          <p className="mt-2 text-sm text-gray-400">Em breve</p>
        </div>
      </div>
    </div>
  )
}
