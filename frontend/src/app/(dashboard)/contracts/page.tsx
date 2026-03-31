'use client'

import { useRouter } from 'next/navigation'
import ContractList from '@/components/contract/ContractList'

export default function ContractsPage() {
  const router = useRouter()

  return (
    <ContractList
      onCreateClick={() => router.push('/contracts/new')}
      onContractClick={(id) => router.push(`/contracts/${id}`)}
    />
  )
}
