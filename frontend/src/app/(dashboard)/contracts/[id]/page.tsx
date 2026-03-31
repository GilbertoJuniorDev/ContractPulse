'use client'

import { useRouter } from 'next/navigation'
import ContractDetail from '@/components/contract/ContractDetail'

export default function ContractDetailPage({
  params,
}: {
  params: { id: string }
}) {
  const router = useRouter()

  return (
    <ContractDetail
      contractId={params.id}
      onBack={() => router.push('/contracts')}
    />
  )
}
