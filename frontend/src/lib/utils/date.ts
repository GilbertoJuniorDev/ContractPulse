/**
 * Utilitários para formatação e manipulação de datas.
 */

/**
 * Formata uma data ISO em formato localizado pt-BR.
 * Exemplo: "01 de jan. de 2025"
 */
export function formatDate(isoDate: string): string {
  return new Date(isoDate).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

/**
 * Formata uma data ISO em formato curto pt-BR.
 * Exemplo: "01/01/2025"
 */
export function formatDateShort(isoDate: string): string {
  return new Date(isoDate).toLocaleDateString('pt-BR')
}

/**
 * Formata uma data ISO incluindo horário pt-BR.
 * Exemplo: "01/01/2025 14:30"
 */
export function formatDateTime(isoDate: string): string {
  return new Date(isoDate).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
