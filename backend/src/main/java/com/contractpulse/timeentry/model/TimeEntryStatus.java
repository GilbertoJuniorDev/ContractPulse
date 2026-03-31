package com.contractpulse.timeentry.model;

/**
 * Status possíveis de um lançamento de horas.
 */
public enum TimeEntryStatus {

    /** Aguardando aprovação do cliente. */
    PENDING,

    /** Aprovado pelo cliente. */
    APPROVED,

    /** Disputado pelo cliente. */
    DISPUTED
}
