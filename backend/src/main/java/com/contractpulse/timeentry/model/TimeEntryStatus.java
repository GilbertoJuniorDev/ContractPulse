package com.contractpulse.timeentry.model;

/**
 * Ciclo de vida de um lançamento de horas.
 * DRAFT → SUBMITTED → PENDING_APPROVAL → APPROVED | DISPUTED → INVOICED
 */
public enum TimeEntryStatus {

    /** Rascunho, não visível ao cliente. */
    DRAFT,

    /** Enviado para revisão pelo provider. */
    SUBMITTED,

    /** Aguardando aprovação do cliente (após ciclo semanal). */
    PENDING_APPROVAL,

    /** Aprovado pelo cliente — entra no cálculo de fatura. */
    APPROVED,

    /** Disputado pelo cliente — abre thread de resolução. */
    DISPUTED,

    /** Incluído em fatura emitida pelo sistema. */
    INVOICED
}
