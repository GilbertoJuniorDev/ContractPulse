/**
 * Tipos e interfaces do módulo de organizações.
 */

export type OrganizationPlan = 'FREE' | 'PRO' | 'ENTERPRISE'

export interface Organization {
  id: string
  name: string
  ownerId: string
  plan: OrganizationPlan
  createdAt: string
  updatedAt: string
}

export interface CreateOrganizationRequest {
  name: string
}

export interface UpdateOrganizationRequest {
  name: string
}
