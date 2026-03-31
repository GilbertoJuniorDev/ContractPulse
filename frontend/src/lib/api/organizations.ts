/**
 * Fetch wrappers para endpoints de organizações no backend Java.
 */

import { apiFetch } from './client'
import type {
  Organization,
  CreateOrganizationRequest,
  UpdateOrganizationRequest,
} from '@/lib/types/organization'

const BASE_PATH = '/api/organizations'

/**
 * Lista todas as organizações do usuário autenticado.
 */
export async function fetchOrganizations(): Promise<Organization[]> {
  return apiFetch<Organization[]>(BASE_PATH)
}

/**
 * Busca uma organização pelo ID.
 */
export async function fetchOrganizationById(id: string): Promise<Organization> {
  return apiFetch<Organization>(`${BASE_PATH}/${id}`)
}

/**
 * Cria uma nova organização.
 */
export async function createOrganization(
  data: CreateOrganizationRequest
): Promise<Organization> {
  return apiFetch<Organization>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * Atualiza uma organização existente.
 */
export async function updateOrganization(
  id: string,
  data: UpdateOrganizationRequest
): Promise<Organization> {
  return apiFetch<Organization>(`${BASE_PATH}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/**
 * Remove uma organização.
 */
export async function deleteOrganization(id: string): Promise<void> {
  return apiFetch<void>(`${BASE_PATH}/${id}`, {
    method: 'DELETE',
  })
}
