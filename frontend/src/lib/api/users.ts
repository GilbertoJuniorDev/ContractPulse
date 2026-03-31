/**
 * Fetch wrappers para endpoints de usuário no backend Java.
 */

import { apiFetch } from './client'

export interface SyncUserRequest {
  fullName: string
  email: string
  avatarUrl: string | null
}

export interface UpdateProfileRequest {
  fullName: string
  avatarUrl: string | null
}

export interface UserResponse {
  id: string
  fullName: string
  email: string
  avatarUrl: string | null
  role: string
  createdAt: string
}

/**
 * Sincroniza o usuário no backend após login via Supabase Auth.
 * Cria o registro local caso não exista, ou atualiza se já existir.
 */
export async function syncUser(data: SyncUserRequest): Promise<UserResponse> {
  return apiFetch<UserResponse>('/api/users/sync', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * Retorna o perfil do usuário autenticado.
 */
export async function fetchCurrentUser(): Promise<UserResponse> {
  return apiFetch<UserResponse>('/api/users/me')
}

/**
 * Atualiza o perfil do usuário autenticado (nome e avatar).
 */
export async function updateProfile(data: UpdateProfileRequest): Promise<UserResponse> {
  return apiFetch<UserResponse>('/api/users/me', {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/**
 * Lista todos os usuários com role CLIENT.
 * Usado no wizard de criação de contrato para selecionar o cliente.
 */
export async function fetchClients(): Promise<UserResponse[]> {
  return apiFetch<UserResponse[]>('/api/users/clients')
}
