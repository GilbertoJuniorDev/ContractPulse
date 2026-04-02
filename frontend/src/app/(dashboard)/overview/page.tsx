/**
 * Visão geral do Provider — dashboard principal com métricas e pipeline.
 */
'use client'

import PendingApprovalsPipeline from '@/components/time-entry/PendingApprovalsPipeline'

export default function OverviewPage() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">
          Visão Geral
        </h1>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Acompanhe o status dos seus contratos e aprovações pendentes.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Pipeline de aprovações pendentes */}
        <PendingApprovalsPipeline />

        {/* Placeholder para faturamento projetado — a ser implementado */}
        <div className="card flex flex-col items-center justify-center border-2 border-dashed border-gray-300 dark:border-dark-border">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 dark:bg-dark-hover">
            <svg className="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 18.75a60.07 60.07 0 0115.797 2.101c.727.198 1.453-.342 1.453-1.096V18.75M3.75 4.5v.75A.75.75 0 013 6h-.75m0 0v-.375c0-.621.504-1.125 1.125-1.125H20.25M2.25 6v9m18-10.5v.75c0 .414.336.75.75.75h.75m-1.5-1.5h.375c.621 0 1.125.504 1.125 1.125v9.75c0 .621-.504 1.125-1.125 1.125h-.375m1.5-1.5H21a.75.75 0 00-.75.75v.75m0 0H3.75m0 0h-.375a1.125 1.125 0 01-1.125-1.125V15m1.5 1.5v-.75A.75.75 0 003 15h-.75M15 10.5a3 3 0 11-6 0 3 3 0 016 0zm3 0h.008v.008H18V10.5zm-12 0h.008v.008H6V10.5z" />
            </svg>
          </div>
          <h3 className="mt-4 text-base font-semibold text-gray-400 dark:text-gray-500">
            Faturamento Projetado
          </h3>
          <p className="mt-1 text-sm text-gray-400 dark:text-gray-500">Em breve</p>
        </div>
      </div>
    </div>
  )
}
