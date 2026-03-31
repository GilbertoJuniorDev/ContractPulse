'use client'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  fetchOrganizations,
  fetchOrganizationById,
  createOrganization,
  updateOrganization,
  deleteOrganization,
} from '@/lib/api/organizations'
import type {
  CreateOrganizationRequest,
  UpdateOrganizationRequest,
} from '@/lib/types/organization'

const ORGANIZATIONS_KEY = ['organizations'] as const

/**
 * Hook para listar todas as organizações do usuário autenticado.
 */
export function useOrganizations() {
  return useQuery({
    queryKey: ORGANIZATIONS_KEY,
    queryFn: fetchOrganizations,
  })
}

/**
 * Hook para buscar uma organização pelo ID.
 */
export function useOrganization(id: string) {
  return useQuery({
    queryKey: [...ORGANIZATIONS_KEY, id],
    queryFn: () => fetchOrganizationById(id),
    enabled: !!id,
  })
}

/**
 * Hook para criar uma nova organização.
 * Invalida o cache da listagem após sucesso.
 */
export function useCreateOrganization() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: CreateOrganizationRequest) => createOrganization(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ORGANIZATIONS_KEY })
    },
  })
}

/**
 * Hook para atualizar uma organização existente.
 * Invalida cache da listagem e do detalhe após sucesso.
 */
export function useUpdateOrganization() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateOrganizationRequest }) =>
      updateOrganization(id, data),
    onSuccess: (_result, variables) => {
      queryClient.invalidateQueries({ queryKey: ORGANIZATIONS_KEY })
      queryClient.invalidateQueries({ queryKey: [...ORGANIZATIONS_KEY, variables.id] })
    },
  })
}

/**
 * Hook para remover uma organização.
 * Invalida o cache da listagem após sucesso.
 */
export function useDeleteOrganization() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deleteOrganization(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ORGANIZATIONS_KEY })
    },
  })
}
