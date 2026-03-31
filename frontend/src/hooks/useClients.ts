'use client'

import { useQuery } from '@tanstack/react-query'
import { fetchClients, type UserResponse } from '@/lib/api/users'

const CLIENTS_KEY = ['users', 'clients'] as const

/**
 * Hook para listar todos os usuários com role CLIENT.
 * Usado no wizard de criação de contrato para selecionar o cliente.
 */
export function useClients() {
  return useQuery<UserResponse[]>({
    queryKey: CLIENTS_KEY,
    queryFn: fetchClients,
  })
}
