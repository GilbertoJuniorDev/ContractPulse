'use client'

import { useRouter } from 'next/navigation'
import ContractWizard from '@/components/contract/ContractWizard'

export default function NewContractPage() {
  const router = useRouter()

  return (
    <div>
      <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">
        Novo Contrato
      </h2>
      <div className="card">
        <ContractWizard
          onSuccess={() => router.push('/contracts')}
          onCancel={() => router.push('/contracts')}
        />
      </div>
    </div>
  )
}
