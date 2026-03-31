/**
 * Utilitários para formatação e conversão de moeda.
 */

import type { ContractCurrency } from '@/lib/types/contract'

const CURRENCY_LOCALE: Record<ContractCurrency, string> = {
  BRL: 'pt-BR',
  USD: 'en-US',
  EUR: 'de-DE',
}

const CURRENCY_LABELS: Record<ContractCurrency, string> = {
  BRL: 'Real (R$)',
  USD: 'Dólar (US$)',
  EUR: 'Euro (€)',
}

/**
 * Formata um valor numérico na moeda indicada.
 */
export function formatCurrency(
  value: number,
  currency: ContractCurrency
): string {
  return new Intl.NumberFormat(CURRENCY_LOCALE[currency], {
    style: 'currency',
    currency,
  }).format(value)
}

/**
 * Retorna o label amigável de uma moeda.
 */
export function getCurrencyLabel(currency: ContractCurrency): string {
  return CURRENCY_LABELS[currency]
}
