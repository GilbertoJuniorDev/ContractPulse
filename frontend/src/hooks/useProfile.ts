'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  fetchCurrentUser,
  updateProfile,
  type UpdateProfileRequest,
  type UserResponse,
} from '@/lib/api/users'

const PROFILE_QUERY_KEY = ['profile', 'me'] as const

/**
 * Hook para buscar e atualizar o perfil do usuário autenticado.
 * Encapsula lógica de cache (React Query) e invalidação automática.
 */
export function useProfile() {
  const queryClient = useQueryClient()

  const profileQuery = useQuery<UserResponse>({
    queryKey: PROFILE_QUERY_KEY,
    queryFn: fetchCurrentUser,
    staleTime: 5 * 60 * 1000,
  })

  const updateMutation = useMutation<UserResponse, Error, UpdateProfileRequest>({
    mutationFn: updateProfile,
    onSuccess: (updatedUser) => {
      queryClient.setQueryData(PROFILE_QUERY_KEY, updatedUser)
    },
  })

  return {
    user: profileQuery.data,
    isLoading: profileQuery.isLoading,
    isError: profileQuery.isError,
    error: profileQuery.error,
    updateProfile: updateMutation.mutateAsync,
    isUpdating: updateMutation.isPending,
    updateError: updateMutation.error,
  }
}
