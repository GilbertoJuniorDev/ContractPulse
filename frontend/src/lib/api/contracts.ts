/**
 * Fetch wrappers para endpoints de contratos no backend Java.
 */

import { apiFetch } from './client'
import type {
  Contract,
  CreateContractRequest,
  UpdateContractRequest,
} from '@/lib/types/contract'

const BASE_PATH = '/api/contracts'

/**
 * Cria um novo contrato.
 */
export async function createContract(
  data: CreateContractRequest
): Promise<Contract> {
  return apiFetch<Contract>(BASE_PATH, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * Busca um contrato pelo ID.
 */
export async function fetchContractById(id: string): Promise<Contract> {
  return apiFetch<Contract>(`${BASE_PATH}/${id}`)
}

/**
 * Lista todos os contratos de uma organização.
 */
export async function fetchContractsByOrganization(
  organizationId: string
): Promise<Contract[]> {
  return apiFetch<Contract[]>(`${BASE_PATH}/organization/${organizationId}`)
}

/**
 * Lista contratos ativos de uma organização.
 */
export async function fetchActiveContractsByOrganization(
  organizationId: string
): Promise<Contract[]> {
  return apiFetch<Contract[]>(
    `${BASE_PATH}/organization/${organizationId}/active`
  )
}

/**
 * Lista contratos vinculados a um cliente.
 */
export async function fetchContractsByClient(
  clientUserId: string
): Promise<Contract[]> {
  return apiFetch<Contract[]>(`${BASE_PATH}/client/${clientUserId}`)
}

/**
 * Atualiza um contrato existente.
 */
export async function updateContract(
  id: string,
  data: UpdateContractRequest
): Promise<Contract> {
  return apiFetch<Contract>(`${BASE_PATH}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/**
 * Pausa um contrato ativo.
 */
export async function pauseContract(id: string): Promise<Contract> {
  return apiFetch<Contract>(`${BASE_PATH}/${id}/pause`, {
    method: 'PATCH',
  })
}

/**
 * Reativa um contrato pausado.
 */
export async function resumeContract(id: string): Promise<Contract> {
  return apiFetch<Contract>(`${BASE_PATH}/${id}/resume`, {
    method: 'PATCH',
  })
}

/**
 * Encerra um contrato.
 */
export async function terminateContract(id: string): Promise<Contract> {
  return apiFetch<Contract>(`${BASE_PATH}/${id}/terminate`, {
    method: 'PATCH',
  })
}
