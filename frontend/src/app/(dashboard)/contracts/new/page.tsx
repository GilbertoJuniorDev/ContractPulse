'use client'

import { useRouter } from 'next/navigation'
import ContractWizard from '@/components/contract/ContractWizard'

export default function NewContractPage() {
  const router = useRouter()

  return (
    <div>
      <h2 className="mb-4 text-lg font-semibold text-gray-900">
        Novo Contrato
      </h2>
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        <ContractWizard
          onSuccess={() => router.push('/contracts')}
          onCancel={() => router.push('/contracts')}
        />
      </div>
    </div>
  )
}
